package net.primal.android.auth

import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val accountsStore: UserAccountsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userAccountFetcher: UserAccountFetcher,
) {

    suspend fun login(nsec: String): String {
        val pubkey = credentialsStore.save(nsec)

        val userProfile = fetchUserProfileOrNulL(pubkey)
        val userContacts = fetchUserContactsOrNulL(pubkey)
        val userAccount = UserAccount.buildLocal(pubkey).merge(
            profile = userProfile,
            contacts = userContacts,
        )

        accountsStore.upsertAccount(userAccount)
        activeAccountStore.setActiveUserId(pubkey)
        return pubkey
    }

    suspend fun logout() {
        credentialsStore.clearCredentials()
        accountsStore.clearAllAccounts()
        activeAccountStore.clearActiveUserAccount()
    }

    private fun UserAccount.merge(profile: UserAccount?, contacts: UserAccount?) = this.copy(
        authorDisplayName = profile?.authorDisplayName ?: contacts?.authorDisplayName ?: this.authorDisplayName,
        userDisplayName = profile?.userDisplayName ?: contacts?.userDisplayName ?: this.userDisplayName,
        pictureUrl = profile?.pictureUrl,
        internetIdentifier = profile?.internetIdentifier,
        followersCount = profile?.followersCount,
        followingCount = profile?.followingCount,
        notesCount = profile?.notesCount,
        relays = contacts?.relays ?: emptyList(),
        following = contacts?.following ?: emptyList(),
        followers = contacts?.followers ?: emptyList(),
        interests = contacts?.interests ?: emptyList(),
    )

    private suspend fun fetchUserProfileOrNulL(pubkey: String) = try {
        userAccountFetcher.fetchUserProfile(pubkey)
    } catch (error: WssException) {
        null
    }

    private suspend fun fetchUserContactsOrNulL(pubkey: String) = try {
        userAccountFetcher.fetchUserContacts(pubkey)
    } catch (error: WssException) {
        null
    }

}
