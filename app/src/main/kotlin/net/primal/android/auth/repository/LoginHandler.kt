package net.primal.android.auth.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.bookmarks.BookmarksRepository
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.assureValidNsec
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.repository.UserRepository

class LoginHandler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mutedUserRepository: MutedUserRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val dispatchers: CoroutineDispatcherProvider,
    private val credentialsStore: CredentialsStore,
) {

    suspend fun login(nostrKey: String) =
        withContext(dispatchers.io()) {
            runCatching {
                val userId = credentialsStore.save(nostrKey = nostrKey)

                userRepository.fetchAndUpdateUserAccount(userId = userId)
                bookmarksRepository.fetchAndPersistPublicBookmarks(userId = userId)
                settingsRepository.fetchAndPersistAppSettings(userId = userId)
                mutedUserRepository.fetchAndPersistMuteList(userId = userId)
            }.onFailure { exception ->
                credentialsStore.removeCredentialByNsec(nsec = nostrKey.assureValidNsec())
                throw exception
            }.onSuccess {
                authRepository.login(nostrKey = nostrKey)
            }
        }
}
