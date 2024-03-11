package net.primal.android.auth.login

import javax.inject.Inject
import net.primal.android.auth.AuthRepository
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.UserRepository

class LoginHandler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mutedUserRepository: MutedUserRepository,
    private val feedRepository: FeedRepository,
) {

    suspend fun loginAndReturnDefaultFeed(nostrKey: String): String {
        val userId = authRepository.login(nostrKey = nostrKey)
        userRepository.fetchAndUpdateUserAccount(userId = userId)
        settingsRepository.fetchAndPersistAppSettings(userId = userId)
        mutedUserRepository.fetchAndPersistMuteList(userId = userId)
        val defaultFeed = feedRepository.defaultFeed()
        return defaultFeed?.directive ?: userId
    }
}
