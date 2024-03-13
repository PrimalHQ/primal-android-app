package net.primal.android.auth.repository

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.crypto.CryptoUtils
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.UserRepository

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) {

    suspend fun createAccountAndLogin(): String {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        return login(nostrKey = keypair.privateKey)
    }

    suspend fun login(nostrKey: String): String {
        val userId = credentialsStore.save(nostrKey)
        userRepository.createNewUserAccount(userId = userId)
        activeAccountStore.setActiveUserId(userId)
        return userId
    }

    suspend fun logout() {
        credentialsStore.clearCredentials()
        userRepository.removeAllUserAccounts()
        activeAccountStore.clearActiveUserAccount()
    }
}
