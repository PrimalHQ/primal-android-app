package net.primal.android.profile.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStatsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.asUserAccountFromContactsEvent
import net.primal.android.user.repository.UserRepository

class ProfileRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val userRepository: UserRepository,
    private val userAccountFetcher: UserAccountFetcher,
) {

    fun findProfileDataOrNull(profileId: String) = database.profiles().findProfileData(profileId = profileId)

    fun observeProfile(profileId: String) =
        database.profiles().observeProfile(
            profileId = profileId,
        ).filterNotNull()

    suspend fun requestProfileUpdate(profileId: String) {
        val response = usersApi.getUserProfile(pubkey = profileId)
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profileMetadata = response.metadata?.asProfileDataPO(cdnResources = cdnResources)
        val profileStats = response.profileStats?.asProfileStatsPO()

        database.withTransaction {
            if (profileMetadata != null) {
                database.profiles().upsertAll(data = listOf(profileMetadata))
            }

            if (profileStats != null) {
                database.profileStats().upsert(data = profileStats)
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

    private suspend fun updateFollowing(userId: String, reducer: Set<String>.() -> Set<String>) {
        val userContacts = userAccountFetcher.fetchUserContactsOrNull(userId = userId) ?: throw WssException()

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
            contactsUserAccount = nostrEventResponse.asUserAccountFromContactsEvent(),
        )
    }
}
