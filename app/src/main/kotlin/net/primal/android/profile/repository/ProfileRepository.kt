package net.primal.android.profile.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.explore.domain.UserProfileSearchItem
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStatsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.takeContentAsPrimalUserFollowersCountsOrNull
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

    fun observeProfile(profileId: String) = database.profiles().observeProfile(profileId = profileId).filterNotNull()

    fun observeProfileData(profileId: String) =
        database.profiles().observeProfileData(profileId = profileId).filterNotNull()

    fun observeProfileStats(profileId: String) =
        database.profileStats().observeProfileStats(profileId = profileId).filterNotNull()

    suspend fun requestProfileUpdate(profileId: String) {
        val response = usersApi.getUserProfile(userId = profileId)
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
        updateFollowList(userId = userId) {
            toMutableSet().apply { add(followedUserId) }
        }
    }

    suspend fun unfollow(userId: String, unfollowedUserId: String) {
        updateFollowList(userId = userId) {
            toMutableSet().apply { remove(unfollowedUserId) }
        }
    }

    private suspend fun updateFollowList(userId: String, reducer: Set<String>.() -> Set<String>) {
        val userContacts = userAccountFetcher.fetchUserFollowListOrNull(userId = userId)
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
            contactsUserAccount = nostrEventResponse.asUserAccountFromContactsEvent(),
        )
    }

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> {
        val response = apiBlock()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = response.contactsMetadata.mapAsProfileDataPO(cdnResources = cdnResources)
        val followersCountsMap = response.followerCounts?.takeContentAsPrimalUserFollowersCountsOrNull()

        database.profiles().upsertAll(data = profiles)

        return profiles.map {
            val score = followersCountsMap?.get(it.ownerId)
            UserProfileSearchItem(metadata = it, followersCount = score)
        }.sortedByDescending { it.followersCount }
    }

    suspend fun fetchFollowers(userId: String) =
        queryRemoteUsers {
            usersApi.getUserFollowers(userId = userId)
        }

    suspend fun fetchFollowing(userId: String) =
        queryRemoteUsers {
            usersApi.getUserFollowing(userId = userId)
        }
}
