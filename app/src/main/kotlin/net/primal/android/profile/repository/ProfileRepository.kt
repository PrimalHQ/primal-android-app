package net.primal.android.profile.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.nostr.ext.asProfileMetadataPO
import net.primal.android.nostr.ext.asProfileStats
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.asUserAccount
import net.primal.android.user.repository.UserRepository
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val userRepository: UserRepository,
    private val userAccountFetcher: UserAccountFetcher,
) {
    fun observeProfile(profileId: String) =
        database.profiles().observeProfile(profileId = profileId).filterNotNull()

    suspend fun requestProfileUpdate(profileId: String) {
        val response = withContext(Dispatchers.IO) { usersApi.getUserProfile(pubkey = profileId) }
        val profileMetadata = response.metadata?.asProfileMetadataPO()
        val userProfileStats = response.profileStats?.takeContentAsUserProfileStatsOrNull()

        withContext(Dispatchers.IO) {
            database.withTransaction {
                if (profileMetadata != null) {
                    database.profiles().upsertAll(profiles = listOf(profileMetadata))
                }

                if (userProfileStats != null) {
                    database.profileStats().upsert(
                        data = userProfileStats.asProfileStats(profileId = profileId)
                    )
                }
            }
        }
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

        val newContactsNostrEvent = usersApi.setUserContacts(
            ownerId = userId,
            contacts = userContacts.following.reducer(),
            relays = userContacts.relays,
        )
        userRepository.updateContacts(userId, newContactsNostrEvent.asUserAccount())
    }
}
