package net.primal.android.auth.repository

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.UserRepository

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) {
    suspend fun login(nostrKey: String): String {
        val userId = credentialsStore.save(nostrKey)
        activeAccountStore.setActiveUserId(userId)
        userRepository.createNewUserAccount(userId = userId)
        return userId
    }

    suspend fun logout() {
        credentialsStore.clearCredentials()
        userRepository.removeAllUserAccounts()
        activeAccountStore.clearActiveUserAccount()
    }
}
