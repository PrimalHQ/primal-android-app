package net.primal.android.wallet.init

import io.github.aakira.napier.Napier
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
                Napier.e(throwable = t) { "disconnectWallet failed for walletId=$walletId" }
            }
            currentWalletId = null
        }
    }

    private suspend fun initializeWalletWithRetry(userId: String) {
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            if (hasUserChanged(userId)) {
                Napier.d { "User changed during retry, aborting initialization for userId=$userId" }
                return
            }

            val result = ensureSparkWalletExistsUseCase.invoke(userId)

            result.onSuccess { walletId ->
                currentWalletId = walletId
                Napier.d { "Wallet initialized for userId=$userId, walletId=$walletId" }
                return
            }

            result.onFailure { t ->
                val remainingAttempts = MAX_RETRY_ATTEMPTS - attempt - 1
                if (remainingAttempts > 0) {
                    val delaySeconds = INITIAL_RETRY_DELAY_SECONDS shl attempt
                    Napier.w(throwable = t) {
                        "initializeWallet failed for userId=$userId, " +
                            "retrying in ${delaySeconds}s ($remainingAttempts attempts left)"
                    }
                    delay(delaySeconds.seconds)
                } else {
                    Napier.e(
                        throwable = t,
                    ) { "initializeWallet failed for userId=$userId after $MAX_RETRY_ATTEMPTS attempts" }
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
