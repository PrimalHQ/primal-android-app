package net.primal.android.auth.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.getOrNull
import net.primal.domain.usecase.EnsurePrimalWalletExistsUseCase

@Suppress("LongParameterList")
class LoginHandler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val ensurePrimalWalletExistsUseCase: EnsurePrimalWalletExistsUseCase,
    private val dispatchers: DispatcherProvider,
    private val credentialsStore: CredentialsStore,
    private val nostrNotary: NostrNotary,
) {
    suspend fun login(
        nostrKey: String,
        credentialType: CredentialType,
        authorizationEvent: NostrEvent?,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val userId = saveCredentials(credentialType = credentialType, nostrKey = nostrKey)
            val authorizationEvent = authorizationEvent ?: nostrNotary.signAuthorizationNostrEvent(
                userId = userId,
                description = "Sync app settings",
            ).getOrNull()

            userRepository.fetchAndUpdateUserAccount(userId = userId)

            ensurePrimalWalletExistsUseCase.invoke(userId = userId, setAsActive = true)

            bookmarksRepository.fetchAndPersistBookmarks(userId = userId)
            authorizationEvent?.let {
                settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
            }
            mutedItemRepository.fetchAndPersistMuteList(userId = userId)
        }.onFailure { exception ->
            removeCredentials(credentialType = credentialType, nostrKey = nostrKey)

            throw exception
        }.onSuccess {
            when (credentialType) {
                CredentialType.ExternalSigner -> authRepository.loginWithExternalSignerNpub(npub = nostrKey)

                CredentialType.PublicKey -> authRepository.loginWithNpub(npub = nostrKey)

                CredentialType.PrivateKey -> authRepository.loginWithNsec(nostrKey = nostrKey)

                CredentialType.InternalSigner -> Unit
            }
        }
    }

    private suspend fun saveCredentials(credentialType: CredentialType, nostrKey: String): String {
        return when (credentialType) {
            CredentialType.ExternalSigner -> credentialsStore.saveExternalSignerNpub(npub = nostrKey)

            CredentialType.PublicKey -> credentialsStore.saveNpub(npub = nostrKey)

            CredentialType.PrivateKey -> credentialsStore.saveNsec(nostrKey = nostrKey)

            CredentialType.InternalSigner -> error("Can't login with InternalSigner key.")
        }
    }

    private suspend fun removeCredentials(credentialType: CredentialType, nostrKey: String) {
        when (credentialType) {
            CredentialType.PublicKey, CredentialType.ExternalSigner ->
                credentialsStore.removeCredentialByNpub(npub = nostrKey)

            CredentialType.PrivateKey -> credentialsStore.removeCredentialByNsec(nsec = nostrKey.assureValidNsec())

            CredentialType.InternalSigner -> Unit
        }
    }
}
