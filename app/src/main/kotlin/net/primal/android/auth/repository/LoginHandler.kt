package net.primal.android.auth.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.bookmarks.BookmarksRepository
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.assureValidNsec
import net.primal.android.nostr.notary.NostrNotary
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
    private val nostrNotary: NostrNotary,
) {
    enum class LoginType {
        Npub,
        Nsec,
    }

    suspend fun login(nostrKey: String, loginType: LoginType) =
        withContext(dispatchers.io()) {
            runCatching {
                val userId = when (loginType) {
                    LoginType.Npub -> credentialsStore.saveNpub(npub = nostrKey)
                    LoginType.Nsec -> credentialsStore.saveNsec(nostrKey = nostrKey)
                }
                val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )

                userRepository.fetchAndUpdateUserAccount(userId = userId)
                bookmarksRepository.fetchAndPersistPublicBookmarks(userId = userId)
                settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
                mutedUserRepository.fetchAndPersistMuteList(userId = userId)
            }.onFailure { exception ->
                when (loginType) {
                    LoginType.Nsec -> credentialsStore.removeCredentialByNsec(nsec = nostrKey.assureValidNsec())
                    LoginType.Npub -> credentialsStore.removeCredentialByNpub(npub = nostrKey)
                }

                throw exception
            }.onSuccess {
                when (loginType) {
                    LoginType.Npub -> authRepository.loginWithNpub(npub = nostrKey)
                    LoginType.Nsec -> authRepository.loginWithNsec(nostrKey = nostrKey)
                }
            }
        }
}
