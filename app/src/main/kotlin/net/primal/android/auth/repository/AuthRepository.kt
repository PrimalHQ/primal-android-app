package net.primal.android.auth.repository

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.UserRepository

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
    private val accountsStore: UserAccountsStore,
) {
    suspend fun loginWithNsec(nostrKey: String): String {
        val userId = credentialsStore.saveNsec(nostrKey)
        activeAccountStore.setActiveUserId(userId)
        return userId
    }

    suspend fun loginWithNpub(npub: String): String {
        val userId = credentialsStore.saveNpub(npub = npub)
        activeAccountStore.setActiveUserId(userId)
        return userId
    }

    suspend fun logout(pubkey: String) {
        if (pubkey == activeAccountStore.activeUserId()) {
            setNextActiveAccount()
        }

        userRepository.removeUserAccountById(pubkey = pubkey)
        credentialsStore.removeCredentialByNpub(npub = pubkey.hexToNpubHrp())
        userRepository.clearAllUserRelatedData(userId = pubkey)
    }

    private suspend fun setNextActiveAccount() {
        val nextActive = accountsStore.userAccounts.value
            .sortedByDescending { it.lastAccessedAt }
            .drop(1)
            .firstOrNull()

        if (nextActive == null) {
            activeAccountStore.clearActiveUserAccount()
        } else {
            activeAccountStore.setActiveUserId(pubkey = nextActive.pubkey)
        }
    }
}
