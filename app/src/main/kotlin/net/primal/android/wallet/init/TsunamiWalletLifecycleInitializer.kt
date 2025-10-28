package net.primal.android.wallet.init

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.TsunamiWalletAccountRepository
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import timber.log.Timber

@Singleton
class TsunamiWalletLifecycleInitializer @Inject constructor(
    dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val credentialsStore: CredentialsStore,
    private val tsunamiWalletAccountRepository: TsunamiWalletAccountRepository,
) {

    private val scope = CoroutineScope(dispatchers.io())

    private var currentWalletId: String? = null

    fun start() {
        scope.launch {
            activeAccountStore.activeUserId
                .map { it.takeIf { id -> id.isNotBlank() } }
                .distinctUntilChanged()
                .collect { userIdOrNull ->
                    currentWalletId?.let { walletId ->
                        runCatching {
                            tsunamiWalletAccountRepository.terminateWallet(walletId).getOrThrow()
                        }.onFailure { t ->
                            Timber.e(t, "terminateWallet failed for walletId=%s", walletId)
                        }
                        currentWalletId = null
                    }

                    val userId = userIdOrNull ?: return@collect

                    val walletKey = runCatching {
                        val userPublicKey = userId.hexToNpubHrp()
                        val credential = credentialsStore.findOrThrow(npub = userPublicKey)
                        if (credential.type == CredentialType.PrivateKey && credential.nsec != null) {
                            credential.nsec
                        } else {
                            userId
                        }
                    }.getOrElse { error ->
                        Timber.w(error, "Falling back to userId as walletKey for userId=%s", userId)
                        userId
                    }

                    runCatching {
                        tsunamiWalletAccountRepository.initializeWallet(
                            userId = userId,
                            walletKey = walletKey,
                        ).getOrThrow()
                    }.onSuccess { walletId ->
                        currentWalletId = walletId

                        runCatching {
                            tsunamiWalletAccountRepository.fetchWalletAccountInfo(userId, walletId)
                        }.onFailure { t ->
                            Timber.w(t, "fetchWalletAccountInfo failed for userId=%s, walletId=%s", userId, walletId)
                        }
                    }.onFailure { t ->
                        Timber.e(t, "initializeWallet failed for userId=%s", userId)
                    }
                }
        }
    }
}
