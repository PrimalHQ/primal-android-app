package net.primal.android.user.repository

import androidx.room.withTransaction
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.premium.repository.asProfileDataDO
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.copyFollowListIfNotNull
import net.primal.android.user.accounts.copyIfNotNull
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.db.Relay
import net.primal.android.user.db.UserProfileInteraction
import net.primal.android.user.db.UsersDatabase
import net.primal.android.user.domain.ContentDisplaySettings
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.domain.asUserAccountFromFollowListEvent
import net.primal.android.wallet.domain.WalletSettings
import net.primal.core.networking.blossom.AndroidPrimalBlossomUploadService
import net.primal.core.networking.blossom.BlossomException
import net.primal.core.networking.blossom.UploadResult
import net.primal.core.networking.nwc.NostrWalletConnect
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.api.users.UsersApi
import net.primal.domain.common.UserProfileSearchItem
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.global.CachingImportRepository
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.user.UserDataCleanupRepository

class UserRepository @Inject constructor(
    private val usersDatabase: UsersDatabase,
    private val dispatchers: DispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val primalUploadService: AndroidPrimalBlossomUploadService,
    private val usersApi: UsersApi,
    private val nostrPublisher: NostrPublisher,
    private val profileRepository: ProfileRepository,
    private val userDataCleanupRepository: UserDataCleanupRepository,
    private val cachingImportRepository: CachingImportRepository,
) {
    suspend fun setActiveAccount(userId: String) =
        withContext(dispatchers.io()) {
            accountsStore.getAndUpdateAccount(userId = userId) { copy(lastAccessedAt = Instant.now().epochSecond) }
            activeAccountStore.setActiveUserId(pubkey = userId)
        }

    fun isNpubLogin(userId: String) =
        runCatching { credentialsStore.isNpubLogin(npub = userId.hexToNpubHrp()) }.getOrDefault(false)

    suspend fun fetchAndUpdateUserAccount(userId: String): UserAccount {
        val userProfile = fetchUserProfileOrNull(userId = userId)
        val userStats = userProfile?.takeIf {
            it.followersCount != null && it.followingCount != null && it.notesCount != null
        }
        val followList = fetchUserFollowListOrNull(userId = userId)

        return accountsStore.getAndUpdateAccount(userId = userId) {
            copyIfNotNull(
                profile = userProfile,
                stats = userStats,
                followList = followList,
            )
        }
    }

    private suspend fun fetchUserProfileOrNull(userId: String): UserAccount? =
        withContext(dispatchers.io()) {
            val userData = profileRepository.fetchProfile(profileId = userId)
                ?: return@withContext null

            val userStats = profileRepository.findProfileStats(profileIds = listOf(userId))
                .firstOrNull { it.profileId == userId }

            UserAccount(
                pubkey = userId,
                authorDisplayName = userData.authorNameUiFriendly(),
                userDisplayName = userData.usernameUiFriendly(),
                avatarCdnImage = userData.avatarCdnImage,
                internetIdentifier = userData.internetIdentifier,
                lightningAddress = userData.lightningAddress,
                followersCount = userStats?.followers,
                followingCount = userStats?.following,
                notesCount = userStats?.notesCount,
                repliesCount = userStats?.repliesCount,
                primalLegendProfile = userData.primalPremiumInfo?.legendProfile,
                blossomServers = userData.blossoms,
            )
        }

    private suspend fun fetchUserFollowListOrNull(userId: String): UserAccount? =
        withContext(dispatchers.io()) {
            val contactsResponse = usersApi.getUserFollowList(userId = userId)

            contactsResponse.followListEvent?.asUserAccountFromFollowListEvent()
        }

    suspend fun clearAllUserRelatedData(userId: String) =
        withContext(dispatchers.io()) {
            userDataCleanupRepository.clearUserData(userId)
            usersDatabase.withTransaction {
                usersDatabase.userProfileInteractions().deleteAllByOwnerId(ownerId = userId)
                usersDatabase.walletTransactions().deleteAllTransactionsByUserId(userId = userId)
                usersDatabase.relays().deleteAll(userId = userId)
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
            usersDatabase.relays().upsertAll(
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
            usersDatabase.relays().deleteAll(userId = userId, kind = RelayKind.NwcRelay)
            accountsStore.getAndUpdateAccount(userId = userId) {
                copy(nostrWallet = null)
            }
        }
    }

    suspend fun removeUserAccountById(pubkey: String) {
        accountsStore.deleteAccount(pubkey = pubkey)
    }

    @Throws(BlossomException::class, NostrPublishException::class, SignatureException::class)
    suspend fun setProfileMetadata(userId: String, profileMetadata: ProfileMetadata) {
        val pictureUrl = profileMetadata.remotePictureUrl
            ?: if (profileMetadata.localPictureUri != null) {
                val uploadResult = primalUploadService.upload(
                    uri = profileMetadata.localPictureUri,
                    userId = userId,
                )
                if (uploadResult is UploadResult.Success) {
                    uploadResult.remoteUrl
                } else {
                    null
                }
            } else {
                null
            }

        val bannerUrl = profileMetadata.remoteBannerUrl
            ?: if (profileMetadata.localBannerUri != null) {
                val uploadResult = primalUploadService.upload(
                    uri = profileMetadata.localBannerUri,
                    userId = userId,
                )
                if (uploadResult is UploadResult.Success) {
                    uploadResult.remoteUrl
                } else {
                    null
                }
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

    @Throws(NostrPublishException::class, NetworkException::class, SignatureException::class)
    suspend fun setNostrAddress(userId: String, nostrAddress: String) =
        withContext(dispatchers.io()) {
            val userProfileResponse = usersApi.getUserProfile(userId = userId)
            val metadata = userProfileResponse.metadata?.content.decodeFromJsonStringOrNull<ContentMetadata>()
                ?: throw NetworkException("Profile Content Metadata not found.")

            setUserProfileAndUpdateLocally(
                userId = userId,
                contentMetadata = metadata.copy(nip05 = nostrAddress),
            )
        }

    @Throws(NostrPublishException::class, NetworkException::class, SignatureException::class)
    suspend fun setLightningAddress(userId: String, lightningAddress: String) =
        withContext(dispatchers.io()) {
            val userProfileResponse = usersApi.getUserProfile(userId = userId)
            val metadata = userProfileResponse.metadata?.content.decodeFromJsonStringOrNull<ContentMetadata>()
                ?: throw NetworkException("Profile Content Metadata not found.")

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
        val profileData = profileMetadataNostrEvent.asProfileDataDO(
            cdnResources = emptyMap(),
            primalUserNames = emptyMap(),
            primalPremiumInfo = emptyMap(),
            primalLegendProfiles = emptyMap(),
            blossomServers = emptyMap(),
        )

        cachingImportRepository.cacheNostrEvents(profileMetadataNostrEvent)
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

    @Throws(FollowListNotFound::class, NostrPublishException::class, SignatureException::class)
    suspend fun follow(
        userId: String,
        followedUserId: String,
        forceUpdate: Boolean,
    ) {
        updateFollowList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { add(followedUserId) }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class, SignatureException::class)
    suspend fun unfollow(
        userId: String,
        unfollowedUserId: String,
        forceUpdate: Boolean,
    ) {
        updateFollowList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { remove(unfollowedUserId) }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class, SignatureException::class)
    private suspend fun updateFollowList(
        userId: String,
        forceUpdate: Boolean,
        reducer: Set<String>.() -> Set<String>,
    ) = withContext(dispatchers.io()) {
        val userFollowList = fetchUserFollowListOrNull(userId = userId)
        val isEmptyFollowList = userFollowList == null || userFollowList.following.isEmpty()
        if (isEmptyFollowList && !forceUpdate) {
            throw FollowListNotFound()
        }

        if (userFollowList != null) {
            updateFollowList(userId, userFollowList)
        }

        val existingFollowing = userFollowList?.following ?: emptySet()
        setFollowList(
            userId = userId,
            contacts = existingFollowing.reducer(),
            content = userFollowList?.followListEventContent ?: "",
        )
    }

    @Throws(NostrPublishException::class, SignatureException::class)
    suspend fun setFollowList(
        userId: String,
        contacts: Set<String>,
        content: String = "",
    ) = withContext(dispatchers.io()) {
        val nostrEventResponse = nostrPublisher.publishUserFollowList(
            userId = userId,
            contacts = contacts,
            content = content,
        )
        updateFollowList(
            userId = userId,
            contactsUserAccount = nostrEventResponse.asUserAccountFromFollowListEvent(),
        )
    }

    @Throws(NostrPublishException::class, SignatureException::class)
    suspend fun recoverFollowList(
        userId: String,
        tags: List<JsonArray>,
        content: String,
    ) = withContext(dispatchers.io()) {
        val publishResult = nostrPublisher.signPublishImportNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.FollowList.value,
                tags = tags,
                content = content,
            ),
        )

        updateFollowList(
            userId = userId,
            contactsUserAccount = publishResult.nostrEvent.asUserAccountFromFollowListEvent(),
        )
    }

    suspend fun markAsInteracted(profileId: String, ownerId: String) =
        withContext(dispatchers.io()) {
            usersDatabase.userProfileInteractions().upsert(
                UserProfileInteraction(
                    profileId = profileId,
                    lastInteractionAt = Instant.now().epochSecond,
                    ownerId = ownerId,
                ),
            )
        }

    fun observeRecentUsers(ownerId: String): Flow<List<UserProfileSearchItem>> =
        usersDatabase.userProfileInteractions()
            .observeRecentProfilesByOwnerId(ownerId)
            .map { recentProfiles ->
                val profileIds = recentProfiles.map { it.profileId }

                val profiles = profileRepository.findProfileData(profileIds).associateBy { it.profileId }
                val statsMap = profileRepository.findProfileStats(profileIds).associateBy { it.profileId }

                profileIds.mapNotNull { profileId ->
                    profiles[profileId]?.let { profile ->
                        val stats = statsMap[profileId]
                        UserProfileSearchItem(metadata = profile, followersCount = stats?.followers)
                    }
                }
            }

    class FollowListNotFound : Exception()
}
