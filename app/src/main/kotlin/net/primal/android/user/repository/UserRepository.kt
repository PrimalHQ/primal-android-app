package net.primal.android.user.repository

import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.copyContactsIfNotNull
import net.primal.android.user.accounts.copyIfNotNull
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userAccountFetcher: UserAccountFetcher,
    private val accountsStore: UserAccountsStore,
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
}
