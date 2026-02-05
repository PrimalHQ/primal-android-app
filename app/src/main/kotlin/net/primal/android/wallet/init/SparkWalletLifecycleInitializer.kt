package net.primal.android.wallet.init

import io.github.aakira.napier.Napier
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.retryOnFailureWithAbort
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import net.primal.domain.wallet.SparkWalletManager
import net.primal.wallet.data.repository.handler.MigratePrimalTransactionsHandler

@Singleton
class SparkWalletLifecycleInitializer @Inject constructor(
    dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val sparkWalletManager: SparkWalletManager,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
    private val migratePrimalTransactionsHandler: MigratePrimalTransactionsHandler,
) {

    private val scope = CoroutineScope(dispatchers.io())

    private var currentWalletId: String? = null

    fun start() {
        scope.launch {
            activeAccountStore.activeUserId
                .map { it.takeIf { id -> id.isNotBlank() } }
                .distinctUntilChanged()
                .collectLatest { userIdOrNull ->
                    disconnectCurrentWalletIfNeeded()

                    val userId = userIdOrNull ?: return@collectLatest
                    initializeWallet(userId)
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

    private suspend fun isEligibleForSparkWallet(userId: String): Boolean {
        val status = primalWalletAccountRepository.fetchWalletStatus(userId).getOrNull() ?: return false
        return !status.hasCustodialWallet || status.hasMigratedToSparkWallet
    }

    private suspend fun initializeWallet(userId: String) {
        try {
            if (!isEligibleForSparkWallet(userId)) {
                Napier.d { "User userId=$userId not eligible for Spark wallet, skipping initialization" }
                return
            }

            suspend { ensureSparkWalletExistsUseCase.invoke(userId) }.retryOnFailureWithAbort(
                times = MAX_RETRY_ATTEMPTS,
                initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
                onRetry = { _, remainingAttempts, delaySeconds, error ->
                    Napier.w(throwable = error) {
                        "initializeWallet failed for userId=$userId, " +
                            "retrying in ${delaySeconds}s ($remainingAttempts attempts left)"
                    }
                },
            ).onFailure { error ->
                Napier.e(throwable = error) { "initializeWallet failed for userId=$userId" }
            }.onSuccess { walletId ->
                currentWalletId = walletId
                Napier.d { "Wallet initialized for userId=$userId, walletId=$walletId" }
                migrateTransactions(userId = userId, walletId = walletId)
            }
        } catch (e: CancellationException) {
            Napier.d { "Wallet initialization cancelled for userId=$userId" }
            throw e
        }
    }

    private suspend fun migrateTransactions(userId: String, walletId: String) {
        migratePrimalTransactionsHandler.invoke(
            userId = userId,
            targetSparkWalletId = walletId,
        ).onSuccess {
            Napier.i { "Background transaction migration completed for walletId=$walletId" }
        }
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 5
        const val INITIAL_RETRY_DELAY_SECONDS = 3
    }
}
