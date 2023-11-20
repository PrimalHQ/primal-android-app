package net.primal.android.user.repository

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.files.FileUploader
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.copyContactsIfNotNull
import net.primal.android.user.accounts.copyIfNotNull
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.UserAccount

class UserRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val userAccountFetcher: UserAccountFetcher,
    private val accountsStore: UserAccountsStore,
    private val fileUploader: FileUploader,
    private val usersApi: UsersApi,
) {

    suspend fun createNewUserAccount(userId: String): UserAccount {
        val account = UserAccount.buildLocal(pubkey = userId)
        accountsStore.upsertAccount(account)
        return account
    }

    suspend fun fetchAndUpdateUserAccount(userId: String): UserAccount {
        val userProfile = userAccountFetcher.fetchUserProfile(pubkey = userId)
        val userContacts = userAccountFetcher.fetchUserContacts(pubkey = userId)
        return accountsStore.getAndUpdateAccount(userId = userId) {
            copyIfNotNull(
                profile = userProfile,
                contacts = userContacts,
            )
        }
    }

    suspend fun updateContacts(userId: String, contactsUserAccount: UserAccount) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copyContactsIfNotNull(contacts = contactsUserAccount)
                .copy(followingCount = contactsUserAccount.following.size)
        }
    }

    suspend fun connectNostrWallet(userId: String, nostrWalletConnect: NostrWallet) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(nostrWallet = nostrWalletConnect)
        }
    }

    suspend fun disconnectNostrWallet(userId: String) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(nostrWallet = null)
        }
    }

    suspend fun removeAllUserAccounts() {
        accountsStore.clearAllAccounts()
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun setProfileMetadata(userId: String, profileMetadata: ProfileMetadata) {
        val pictureUrl = if (profileMetadata.localPictureUri != null) {
            fileUploader.uploadFile(userId = userId, uri = profileMetadata.localPictureUri)
        } else {
            profileMetadata.remotePictureUrl
        }

        val bannerUrl = if (profileMetadata.localBannerUri != null) {
            fileUploader.uploadFile(userId = userId, uri = profileMetadata.localBannerUri)
        } else {
            profileMetadata.remoteBannerUrl
        }

        withContext(Dispatchers.IO) {
            val profileMetadataNostrEvent = usersApi.setUserProfile(
                ownerId = userId,
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

            val profileData = profileMetadataNostrEvent.asProfileDataPO(cdnResources = emptyMap())
            database.profiles().upsertAll(data = listOf(profileData))

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
    }
}
