package net.primal.android.wallet.init

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import net.primal.domain.wallet.SparkWalletManager
import timber.log.Timber

@Singleton
class SparkWalletLifecycleInitializer @Inject constructor(
    dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val sparkWalletManager: SparkWalletManager,
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
) {

    private val scope = CoroutineScope(dispatchers.io())

    private val walletMutex = Mutex()
    private var currentWalletId: String? = null

    fun start() {
        scope.launch {
            activeAccountStore.activeUserId
                .map { it.takeIf { id -> id.isNotBlank() } }
                .distinctUntilChanged()
                .collect { userIdOrNull ->
                    walletMutex.withLock {
                        disconnectCurrentWalletIfNeeded()

                        val userId = userIdOrNull ?: return@withLock
                        initializeWalletForUser(userId)
                    }
                }
        }
    }

    private suspend fun disconnectCurrentWalletIfNeeded() {
        currentWalletId?.let { walletId ->
            runCatching {
                sparkWalletManager.disconnectWallet(walletId).getOrThrow()
            }.onFailure { t ->
                Timber.e(t, "disconnectWallet failed for walletId=%s", walletId)
            }
            currentWalletId = null
        }
    }

    private suspend fun initializeWalletForUser(userId: String) {
        ensureSparkWalletExistsUseCase.invoke(userId)
            .onSuccess { walletId ->
                currentWalletId = walletId
                Timber.d("Wallet initialized for userId=%s, walletId=%s", userId, walletId)
            }
            .onFailure { t ->
                Timber.e(t, "initializeWallet failed for userId=%s", userId)
            }
    }
}
