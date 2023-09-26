package net.primal.android.profile.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.core.files.FileUploader
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStats
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.asUserAccount
import net.primal.android.user.repository.UserRepository
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val userRepository: UserRepository,
    private val userAccountFetcher: UserAccountFetcher,
    private val fileUploader: FileUploader,
) {
    fun observeProfile(profileId: String) =
        database.profiles().observeProfile(profileId = profileId).filterNotNull()

    suspend fun requestProfileUpdate(profileId: String) {
        val response = withContext(Dispatchers.IO) { usersApi.getUserProfile(pubkey = profileId) }
        val profileMetadata = response.metadata?.asProfileDataPO()
        val userProfileStats = response.profileStats?.takeContentAsUserProfileStatsOrNull()

        withContext(Dispatchers.IO) {
            database.withTransaction {
                if (profileMetadata != null) {
                    database.profiles().upsertAll(data = listOf(profileMetadata))
                }

                if (userProfileStats != null) {
                    database.profileStats().upsert(
                        data = userProfileStats.asProfileStats(profileId = profileId)
                    )
                }
            }
        }
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun setProfileMetadata(userId: String, profileMetadata: ProfileMetadata) {
        val pictureUrl = if (profileMetadata.picture != null) {
            fileUploader.uploadFile(userId = userId, uri = profileMetadata.picture)
        } else null

        val bannerUrl = if (profileMetadata.banner != null) {
            fileUploader.uploadFile(userId = userId, uri = profileMetadata.banner)
        } else null

        usersApi.setUserProfile(
            ownerId = userId,
            contentMetadata = ContentMetadata(
                displayName = profileMetadata.displayName,
                name = profileMetadata.handle,
                website = profileMetadata.website,
                about = profileMetadata.about,
                lud16 = profileMetadata.lightningAddress,
                nip05 = profileMetadata.nostrVerification,
                picture = pictureUrl,
                banner = bannerUrl,
            ),
        )
    }

    suspend fun follow(userId: String, followedUserId: String) {
        updateFollowing(userId = userId) {
            toMutableSet().apply { add(followedUserId) }
        }
    }

    suspend fun unfollow(userId: String, unfollowedUserId: String) {
        updateFollowing(userId = userId) {
            toMutableSet().apply { remove(unfollowedUserId) }
        }
    }

    private suspend fun updateFollowing(
        userId: String,
        reducer: Set<String>.() -> Set<String>
    ) {
        val userContacts = userAccountFetcher.fetchUserContacts(pubkey = userId)
            ?: throw MissingRelaysException()

        userRepository.updateContacts(userId, userContacts)

        setContactsAndRelays(
            userId = userId,
            contacts = userContacts.following.reducer(),
            relays = userContacts.relays,
        )
    }

    suspend fun setContactsAndRelays(
        userId: String,
        contacts: Set<String>,
        relays: List<Relay>,
    ) {
        val nostrEventResponse = usersApi.setUserContacts(
            ownerId = userId,
            contacts = contacts,
            relays = relays,
        )
        userRepository.updateContacts(
            userId = userId,
            contactsUserAccount = nostrEventResponse.asUserAccount(),
        )
    }
}
