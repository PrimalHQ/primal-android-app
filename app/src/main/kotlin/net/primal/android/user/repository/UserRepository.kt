package net.primal.android.user.repository

import androidx.room.withTransaction
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.copyFollowListIfNotNull
import net.primal.android.user.accounts.copyIfNotNull
import net.primal.android.user.api.UsersApi
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.db.Relay
import net.primal.android.user.domain.ContentDisplaySettings
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.domain.WalletSettings
import net.primal.networking.sockets.errors.WssException

class UserRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val dispatchers: CoroutineDispatcherProvider,
    private val userAccountFetcher: UserAccountFetcher,
    private val accountsStore: UserAccountsStore,
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val fileUploader: PrimalFileUploader,
    private val usersApi: UsersApi,
    private val nostrPublisher: NostrPublisher,
) {
    suspend fun setActiveAccount(userId: String) =
        withContext(dispatchers.io()) {
            accountsStore.getAndUpdateAccount(userId = userId) { copy(lastAccessedAt = Instant.now().epochSecond) }
            activeAccountStore.setActiveUserId(pubkey = userId)
        }

    fun isNpubLogin(userId: String) =
        runCatching { credentialsStore.isNpubLogin(npub = userId.hexToNpubHrp()) }.getOrDefault(false)

    suspend fun fetchAndUpdateUserAccount(userId: String): UserAccount {
        val userProfile = userAccountFetcher.fetchUserProfileOrNull(userId = userId)
        val userStats = userProfile?.takeIf {
            it.followersCount != null && it.followingCount != null && it.notesCount != null
        }
        val followList = userAccountFetcher.fetchUserFollowListOrNull(userId = userId)

        return accountsStore.getAndUpdateAccount(userId = userId) {
            copyIfNotNull(
                profile = userProfile,
                stats = userStats,
                followList = followList,
            )
        }
    }

    suspend fun clearAllUserRelatedData(userId: String) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                database.messages().deleteAllByOwnerId(ownerId = userId)
                database.messageConversations().deleteAllByOwnerId(ownerId = userId)
                database.feeds().deleteAllByOwnerId(ownerId = userId)
                database.mutedUsers().deleteAllByOwnerId(ownerId = userId)
                database.profileInteractions().deleteAllByOwnerId(ownerId = userId)
                database.walletTransactions().deleteAllTransactionsByUserId(userId = userId)
                database.notifications().deleteAllByOwnerId(ownerId = userId)
                database.articleFeedsConnections().deleteConnections(ownerId = userId)
                database.feedsConnections().deleteConnections(ownerId = userId)
                database.feedPostsRemoteKeys().deleteAllByOwnerId(ownerId = userId)
                database.publicBookmarks().deleteAllBookmarks(userId = userId)
                database.relays().deleteAll(userId = userId)
            }
        }

    suspend fun updateFollowList(userId: String, contactsUserAccount: UserAccount) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copyFollowListIfNotNull(followList = contactsUserAccount)
                .copy(followingCount = contactsUserAccount.following.size)
        }
    }

    suspend fun connectNostrWallet(userId: String, nostrWalletConnect: NostrWalletConnect) {
        withContext(dispatchers.io()) {
            database.relays().upsertAll(
                relays = nostrWalletConnect.relays.map {
                    Relay(userId = userId, kind = RelayKind.NwcRelay, url = it, read = false, write = true)
                },
            )
            accountsStore.getAndUpdateAccount(userId = userId) {
                copy(nostrWallet = nostrWalletConnect)
            }
        }
    }

    suspend fun disconnectNostrWallet(userId: String) {
        withContext(dispatchers.io()) {
            database.relays().deleteAll(userId = userId, kind = RelayKind.NwcRelay)
            accountsStore.getAndUpdateAccount(userId = userId) {
                copy(nostrWallet = null)
            }
        }
    }

    suspend fun removeUserAccountById(pubkey: String) {
        accountsStore.deleteAccount(pubkey = pubkey)
    }

    @Throws(UnsuccessfulFileUpload::class, NostrPublishException::class, MissingPrivateKeyException::class)
    suspend fun setProfileMetadata(userId: String, profileMetadata: ProfileMetadata) {
        val pictureUrl = profileMetadata.remotePictureUrl
            ?: if (profileMetadata.localPictureUri != null) {
                fileUploader.uploadFile(userId = userId, uri = profileMetadata.localPictureUri).remoteUrl
            } else {
                null
            }

        val bannerUrl = profileMetadata.remoteBannerUrl
            ?: if (profileMetadata.localBannerUri != null) {
                fileUploader.uploadFile(userId = userId, uri = profileMetadata.localBannerUri).remoteUrl
            } else {
                null
            }

        setUserProfileAndUpdateLocally(
            userId = userId,
            contentMetadata = ContentMetadata(
                displayName = profileMetadata.displayName,
                name = profileMetadata.username,
                website = profileMetadata.website,
                about = profileMetadata.about,
                lud16 = profileMetadata.lightningAddress,
                nip05 = profileMetadata.nostrVerification,
                picture = pictureUrl,
                banner = bannerUrl,
            ),
        )
    }

    @Throws(NostrPublishException::class, WssException::class, MissingPrivateKeyException::class)
    suspend fun setNostrAddress(userId: String, nostrAddress: String) =
        withContext(dispatchers.io()) {
            val userProfileResponse = usersApi.getUserProfile(userId = userId)
            val metadata = NostrJson.decodeFromStringOrNull<ContentMetadata>(userProfileResponse.metadata?.content)
                ?: throw WssException("Profile Content Metadata not found.")

            setUserProfileAndUpdateLocally(
                userId = userId,
                contentMetadata = metadata.copy(nip05 = nostrAddress),
            )
        }

    @Throws(NostrPublishException::class, WssException::class, MissingPrivateKeyException::class)
    suspend fun setLightningAddress(userId: String, lightningAddress: String) =
        withContext(dispatchers.io()) {
            val userProfileResponse = usersApi.getUserProfile(userId = userId)
            val metadata = NostrJson.decodeFromStringOrNull<ContentMetadata>(userProfileResponse.metadata?.content)
                ?: throw WssException("Profile Content Metadata not found.")

            setUserProfileAndUpdateLocally(
                userId = userId,
                contentMetadata = metadata.copy(lud16 = lightningAddress),
            )
        }

    private suspend fun setUserProfileAndUpdateLocally(userId: String, contentMetadata: ContentMetadata) {
        val profileMetadataNostrEvent = nostrPublisher.publishUserProfile(
            userId = userId,
            contentMetadata = contentMetadata,
        )
        val profileData = profileMetadataNostrEvent.asProfileDataPO(
            cdnResources = emptyMap(),
            primalUserNames = emptyMap(),
            primalPremiumInfo = emptyMap(),
            primalLegendProfiles = emptyMap(),
            blossomServers = emptyMap(),
        )
        database.profiles().insertOrUpdateAll(data = listOf(profileData))

        accountsStore.getAndUpdateAccount(userId = userId) {
            this.copy(
                authorDisplayName = profileData.authorNameUiFriendly(),
                userDisplayName = profileData.usernameUiFriendly(),
                avatarCdnImage = profileData.avatarCdnImage,
                internetIdentifier = profileData.internetIdentifier,
                lightningAddress = profileData.lightningAddress,
            )
        }
    }

    suspend fun updateWalletPreference(userId: String, walletPreference: WalletPreference) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(walletPreference = walletPreference)
        }
    }

    suspend fun updatePrimalWalletBalance(
        userId: String,
        balanceInBtc: String,
        maxBalanceInBtc: String? = null,
    ) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(
                primalWalletState = this.primalWalletState.copy(balanceInBtc = balanceInBtc),
                primalWalletSettings = this.primalWalletSettings.copy(
                    maxBalanceInBtc = maxBalanceInBtc ?: this.primalWalletSettings.maxBalanceInBtc,
                ),
            )
        }
    }

    suspend fun updatePrimalWalletLastUpdatedAt(userId: String, lastUpdatedAt: Long) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(primalWalletState = this.primalWalletState.copy(lastUpdatedAt = lastUpdatedAt))
        }
    }

    suspend fun updatePrimalWalletSettings(userId: String, reducer: WalletSettings.() -> WalletSettings) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(primalWalletSettings = this.primalWalletSettings.reducer())
        }
    }

    suspend fun updateContentDisplaySettings(
        userId: String,
        reducer: ContentDisplaySettings.() -> ContentDisplaySettings,
    ) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(contentDisplaySettings = this.contentDisplaySettings.reducer())
        }
    }

    suspend fun updateCachingProxyEnabled(userId: String, enabled: Boolean) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(cachingProxyEnabled = enabled)
        }
    }

    suspend fun updateBuyPremiumTimestamp(userId: String) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(lastBuyPremiumTimestampInMillis = Clock.System.now().toEpochMilliseconds())
        }
    }
}
