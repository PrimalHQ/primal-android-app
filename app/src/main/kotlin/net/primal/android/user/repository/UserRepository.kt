package net.primal.android.user.repository

import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.copyContactsIfNotNull
import net.primal.android.user.accounts.copyIfNotNull
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccount
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userAccountFetcher: UserAccountFetcher,
    private val accountsStore: UserAccountsStore,
) {

    suspend fun fetchAndUpsertUserAccount(userId: String) {
        val userProfile = userAccountFetcher.fetchUserProfileOrNull(pubkey = userId)
        val userContacts = userAccountFetcher.fetchUserContactsOrNull(pubkey = userId)

        val currentUserAccount = accountsStore.findByIdOrNull(pubkey = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(
            userAccount = currentUserAccount.copyIfNotNull(
                profile = userProfile,
                contacts = userContacts,
            )
        )
    }

    suspend fun updateContacts(userId: String, contactsNostrEvent: NostrEvent) {
        val currentUserAccount = accountsStore.findByIdOrNull(pubkey = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(
            userAccount = currentUserAccount.copyContactsIfNotNull(
                contacts = contactsNostrEvent.asUserAccount()
            ).copy(
                followingCount = contactsNostrEvent.tags?.count { it.isPubKeyTag() }
            )
        )
    }

    suspend fun connectNostrWallet(userId: String, nostrWalletConnect: NostrWallet) {
        val currentUserAccount = accountsStore.findByIdOrNull(pubkey = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(
            userAccount = currentUserAccount.copy(
                nostrWallet = nostrWalletConnect
            )
        )
    }

    suspend fun disconnectNostrWallet(userId: String) {
        val currentUserAccount = accountsStore.findByIdOrNull(pubkey = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(
            userAccount = currentUserAccount.copy(
                nostrWallet = null
            )
        )
    }

    suspend fun removeAllUserAccounts() {
        accountsStore.clearAllAccounts()
    }
}
