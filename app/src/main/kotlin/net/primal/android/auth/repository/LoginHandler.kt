package net.primal.android.auth.repository

import javax.inject.Inject
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.UserRepository

class LoginHandler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mutedUserRepository: MutedUserRepository,
) {

    suspend fun login(nostrKey: String) {
        val userId = authRepository.login(nostrKey = nostrKey)
        val postLoginResult = runCatching {
            userRepository.fetchAndUpdateUserAccount(userId = userId)
            settingsRepository.fetchAndPersistAppSettings(userId = userId)
            mutedUserRepository.fetchAndPersistMuteList(userId = userId)
        }

        val exception = postLoginResult.exceptionOrNull()
        if (exception != null) {
            authRepository.logout()
            throw exception
        }
    }
}
