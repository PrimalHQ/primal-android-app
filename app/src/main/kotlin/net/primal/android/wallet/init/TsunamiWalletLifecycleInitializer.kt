package net.primal.android.wallet.init

import io.github.aakira.napier.Napier
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
                            Napier.e("terminateWallet failed for walletId=$walletId", t)
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
                        Napier.w("Falling back to userId as walletKey for userId=$userId", error)
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
                            Napier.w("fetchWalletAccountInfo failed for userId=$userId, walletId=$walletId", t)
                        }
                    }.onFailure { t ->
                        Napier.e("initializeWallet failed for userId=$userId", t)
                    }
                }
        }
    }
}
