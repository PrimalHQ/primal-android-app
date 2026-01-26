package net.primal.android.wallet.init

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
                        initializeWalletWithRetry(userId)
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

    private suspend fun initializeWalletWithRetry(userId: String) {
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            if (hasUserChanged(userId)) {
                Timber.d("User changed during retry, aborting initialization for userId=%s", userId)
                return
            }

            val result = ensureSparkWalletExistsUseCase.invoke(userId)

            result.onSuccess { walletId ->
                currentWalletId = walletId
                Timber.d("Wallet initialized for userId=%s, walletId=%s", userId, walletId)
                return
            }

            result.onFailure { t ->
                val remainingAttempts = MAX_RETRY_ATTEMPTS - attempt - 1
                if (remainingAttempts > 0) {
                    val delaySeconds = INITIAL_RETRY_DELAY_SECONDS shl attempt
                    Timber.w(
                        t,
                        "initializeWallet failed for userId=%s, retrying in %ds (%d attempts left)",
                        userId,
                        delaySeconds,
                        remainingAttempts,
                    )
                    delay(delaySeconds.seconds)
                } else {
                    Timber.e(t, "initializeWallet failed for userId=%s after %d attempts", userId, MAX_RETRY_ATTEMPTS)
                }
            }
        }
    }

    private suspend fun hasUserChanged(expectedUserId: String): Boolean {
        val currentUserId = activeAccountStore.activeUserId.first()
        return currentUserId != expectedUserId
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 5
        const val INITIAL_RETRY_DELAY_SECONDS = 3
    }
}
