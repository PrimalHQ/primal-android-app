package net.primal.android.auth.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.getOrNull

class LoginHandler @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
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
            val userId = when (credentialType) {
                CredentialType.PublicKey, CredentialType.ExternalSigner ->
                    credentialsStore.saveNpub(npub = nostrKey, credentialType = credentialType)

                CredentialType.PrivateKey -> credentialsStore.saveNsec(nostrKey = nostrKey)

                CredentialType.InternalSigner -> error("Can't login with InternalSigner key.")
            }
            val authorizationEvent = authorizationEvent ?: nostrNotary.signAuthorizationNostrEvent(
                userId = userId,
                description = "Sync app settings",
            ).getOrNull()

            userRepository.fetchAndUpdateUserAccount(userId = userId)

            val primalWalletId = primalWalletAccountRepository.fetchWalletAccountInfo(userId = userId)
            primalWalletId.getOrNull()?.let { walletAccountRepository.setActiveWallet(userId = userId, walletId = it) }

            bookmarksRepository.fetchAndPersistBookmarks(userId = userId)
            authorizationEvent?.let {
                settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
            }
            mutedItemRepository.fetchAndPersistMuteList(userId = userId)
        }.onFailure { exception ->
            when (credentialType) {
                CredentialType.PublicKey, CredentialType.ExternalSigner ->
                    credentialsStore.removeCredentialByNpub(npub = nostrKey)

                CredentialType.PrivateKey -> credentialsStore.removeCredentialByNsec(nsec = nostrKey.assureValidNsec())

                CredentialType.InternalSigner -> Unit
            }

            throw exception
        }.onSuccess {
            when (credentialType) {
                CredentialType.PublicKey, CredentialType.ExternalSigner ->
                    authRepository.loginWithNpub(npub = nostrKey, credentialType = credentialType)

                CredentialType.PrivateKey -> authRepository.loginWithNsec(nostrKey = nostrKey)

                CredentialType.InternalSigner -> Unit
            }
        }
    }
}
