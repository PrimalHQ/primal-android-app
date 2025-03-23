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
import net.primal.android.user.domain.LoginType
import net.primal.android.user.repository.UserRepository
import net.primal.domain.nostr.NostrEvent

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
    suspend fun login(
        nostrKey: String,
        loginType: LoginType,
        authorizationEvent: NostrEvent?,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val userId = when (loginType) {
                LoginType.PublicKey, LoginType.ExternalSigner ->
                    credentialsStore.saveNpub(npub = nostrKey, loginType = loginType)

                LoginType.PrivateKey -> credentialsStore.saveNsec(nostrKey = nostrKey)
            }
            val authorizationEvent = authorizationEvent ?: runCatching {
                nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )
            }.getOrNull()

            userRepository.fetchAndUpdateUserAccount(userId = userId)
            bookmarksRepository.fetchAndPersistPublicBookmarks(userId = userId)
            authorizationEvent?.let {
                settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
            }
            mutedUserRepository.fetchAndPersistMuteList(userId = userId)
        }.onFailure { exception ->
            when (loginType) {
                LoginType.PublicKey, LoginType.ExternalSigner ->
                    credentialsStore.removeCredentialByNpub(npub = nostrKey)

                LoginType.PrivateKey -> credentialsStore.removeCredentialByNsec(nsec = nostrKey.assureValidNsec())
            }

            throw exception
        }.onSuccess {
            when (loginType) {
                LoginType.PublicKey, LoginType.ExternalSigner ->
                    authRepository.loginWithNpub(npub = nostrKey, loginType = loginType)

                LoginType.PrivateKey -> authRepository.loginWithNsec(nostrKey = nostrKey)
            }
        }
    }
}
