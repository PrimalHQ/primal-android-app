package net.primal.android.profile.repository

import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.api.UsersApi
import javax.inject.Inject

class LatestFollowingResolver @Inject constructor(
    private val usersApi: UsersApi,
    private val activeAccountStore: ActiveAccountStore,
) {

    suspend fun getLatestFollowing(): Set<String> {
        val activeAccount = activeAccountStore.activeUserAccount()
        val contactsResponse = usersApi.getUserContacts(
            pubkey = activeAccount.pubkey,
            extendedResponse = false,
        )

        if (contactsResponse.contactsEvent == null) {
            throw RemoteFollowingsUnavailableException()
        }

        return compareAndReturnLatestFollowing(
            localCreatedAt = activeAccount.contactsCreatedAt,
            localFollowings = activeAccount.following,
            remoteCreatedAt = contactsResponse.contactsEvent.createdAt,
            remoteFollowings = contactsResponse.contactsEvent.tags?.parseFollowings() ?: emptySet(),
        )
    }

    private fun compareAndReturnLatestFollowing(
        localCreatedAt: Long?,
        localFollowings: Set<String>,
        remoteCreatedAt: Long,
        remoteFollowings: Set<String>,
    ): Set<String> = when {
        localCreatedAt == null -> remoteFollowings
        localCreatedAt >= remoteCreatedAt -> localFollowings
        else -> remoteFollowings
    }

    inner class RemoteFollowingsUnavailableException : RuntimeException()

}
