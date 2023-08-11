package net.primal.android.auth

import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) {

    suspend fun login(nostrKey: String): String {
        val pubkey = credentialsStore.save(nostrKey)
        userRepository.fetchAndUpsertUserAccount(userId = pubkey)
        activeAccountStore.setActiveUserId(pubkey)
        return pubkey
    }

    suspend fun logout() {
        credentialsStore.clearCredentials()
        userRepository.removeAllUserAccounts()
        activeAccountStore.clearActiveUserAccount()
    }

}
