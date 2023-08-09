package net.primal.android.profile.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asProfileMetadataPO
import net.primal.android.nostr.ext.asProfileStats
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.asUserAccount
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val userAccountsStore: UserAccountsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val latestFollowingResolver: LatestFollowingResolver,
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

    suspend fun follow(followedPubkey: String) {
        val latestFollowing = latestFollowingResolver.getLatestFollowing()
        val updatedFollowing = latestFollowing.toMutableSet().apply { add(followedPubkey) }
        val activeAccount = activeAccountStore.activeUserAccount.value
        val newContactsNostrEvent = usersApi.setUserContacts(
            ownerId = activeAccount.pubkey,
            contacts = updatedFollowing,
            relays = activeAccount.relays
        )
        userAccountsStore.upsertAccount(userAccount = newContactsNostrEvent.asUserAccount())
    }

    suspend fun unfollow(unfollowedPubkey: String) {
        val latestFollowing = latestFollowingResolver.getLatestFollowing()
        val updatedFollowing = latestFollowing.toMutableSet().apply { remove(unfollowedPubkey) }
        val activeAccount = activeAccountStore.activeUserAccount.value
        val newContactsNostrEvent = usersApi.setUserContacts(
            ownerId = activeAccount.pubkey,
            contacts = updatedFollowing,
            relays = activeAccount.relays
        )
        userAccountsStore.upsertAccount(userAccount = newContactsNostrEvent.asUserAccount())
    }
}
