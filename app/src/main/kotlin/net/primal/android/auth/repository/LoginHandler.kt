package net.primal.android.auth.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.LoginType
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.TsunamiWalletAccountRepository
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
    private val tsunamiWalletAccountRepository: TsunamiWalletAccountRepository,
    private val dispatchers: DispatcherProvider,
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
            val authorizationEvent = authorizationEvent ?: nostrNotary.signAuthorizationNostrEvent(
                userId = userId,
                description = "Sync app settings",
            ).getOrNull()

            userRepository.fetchAndUpdateUserAccount(userId = userId)

            val primalWalletId = primalWalletAccountRepository.fetchWalletAccountInfo(userId = userId)
            val tsunamiKey = if (loginType == LoginType.PrivateKey) nostrKey else userId
            val tsunamiWalletId = tsunamiWalletAccountRepository.createWallet(userId = userId, walletKey = tsunamiKey)
            val walletId = tsunamiWalletId.getOrNull() ?: primalWalletId.getOrNull()
            walletId?.let { walletAccountRepository.setActiveWallet(userId = userId, walletId = it) }

            bookmarksRepository.fetchAndPersistBookmarks(userId = userId)
            authorizationEvent?.let {
                settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
            }
            mutedItemRepository.fetchAndPersistMuteList(userId = userId)
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
