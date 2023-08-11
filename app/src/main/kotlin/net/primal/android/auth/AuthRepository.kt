package net.primal.android.auth

import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.merge
import net.primal.android.user.accounts.active.ActiveAccountStore
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

    suspend fun login(nostrKey: String): String {
        val pubkey = credentialsStore.save(nostrKey)

        val userProfile = fetchUserProfileOrNull(pubkey)
        val userContacts = fetchUserContactsOrNull(pubkey)
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

    private suspend fun fetchUserProfileOrNull(pubkey: String) = try {
        userAccountFetcher.fetchUserProfile(pubkey)
    } catch (error: WssException) {
        null
    }

    private suspend fun fetchUserContactsOrNull(pubkey: String) = try {
        userAccountFetcher.fetchUserContacts(pubkey)
    } catch (error: WssException) {
        null
    }

}
