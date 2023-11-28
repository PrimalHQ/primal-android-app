package net.primal.android.auth

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.crypto.CryptoUtils
import net.primal.android.networking.relays.RelaysBootstrapper
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.UserRepository

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
    private val relaysBootstrapper: RelaysBootstrapper,
) {

    suspend fun createAccountAndLogin(): String {
        val keypair = CryptoUtils.generateHexEncodedKeypair()
        val userId = login(nostrKey = keypair.privateKey)
        relaysBootstrapper.bootstrap(userId = userId)
        return userId
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
