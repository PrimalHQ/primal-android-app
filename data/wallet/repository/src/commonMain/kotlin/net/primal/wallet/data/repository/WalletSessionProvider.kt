package net.primal.wallet.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.retryOnFailureWithAbort
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.Wallet
import net.primal.wallet.data.repository.handler.MigratePrimalTransactionsHandler

@Suppress("LongParameterList")
class WalletSessionProvider internal constructor(
    dispatchers: DispatcherProvider,
    private val sparkWalletManager: SparkWalletManager,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
    private val migratePrimalTransactionsHandler: MigratePrimalTransactionsHandler,
    private val walletAccountRepository: WalletAccountRepository,
) {

    private val scope = CoroutineScope(dispatchers.io())

    private val activeUserId = MutableStateFlow<String?>(null)

    private var currentWalletId: String? = null

    fun start() {
        scope.launch {
            activeUserId
                .collectLatest { userIdOrNull ->
                    disconnectCurrentWalletIfNeeded()

                    val userId = userIdOrNull ?: return@collectLatest
                    initializeWallet(userId)
                }
        }

        scope.launch {
            activeUserId
                .collectLatest { userIdOrNull ->
                    val userId = userIdOrNull ?: return@collectLatest
                    walletAccountRepository.observeActiveWallet(userId)
                        .collect { userWallet ->
                            val wallet = userWallet?.wallet ?: return@collect
                            if (wallet is Wallet.Spark) {
                                ensureSdkInitialized(wallet.walletId)
                            }
                        }
                }
        }
    }

    fun setActiveUserId(userId: String?) {
        activeUserId.value = userId?.takeIf { it.isNotBlank() }
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

    private suspend fun ensureSdkInitialized(walletId: String) {
        if (sparkWalletManager.hasInstance(walletId)) return

        val seedWords = sparkWalletAccountRepository
            .getPersistedSeedWords(walletId)
            .getOrNull()
            ?.joinToString(separator = " ")
            ?: return

        sparkWalletManager.initializeWallet(seedWords)
            .onFailure { error ->
                Napier.e(throwable = error) { "ensureSdkInitialized failed for walletId=$walletId" }
            }
    }

    private suspend fun initializeWallet(userId: String) {
        try {
            val userWalletStatus = primalWalletAccountRepository.fetchWalletStatus(userId).getOrNull()
            val hasLocalWallet = sparkWalletAccountRepository.hasPersistedSparkWallet(userId)

            when {
                hasLocalWallet -> {
                    val shouldRegister = userWalletStatus?.isEligibleToRegisterSparkWallet == true
                    ensureWalletAndConnect(userId, register = shouldRegister)
                }

                userWalletStatus?.hasMigratedToSparkWallet == true -> {
                    Napier.d { "Spark wallet detected on backend for userId=$userId, skipping auto-create." }
                }

                userWalletStatus?.hasCustodialWallet == true -> {
                    Napier.d { "Custodial wallet found for userId=$userId, skipping Spark wallet creation." }
                }

                userWalletStatus == null -> {
                    Napier.w {
                        "Cannot determine wallet status for userId=$userId, " +
                            "skipping wallet initialization to prevent accidental wallet creation."
                    }
                }

                else -> {
                    Napier.d {
                        "Initialize wallet in else branch for userId=$userId.\n" +
                            "userWalletStatus=$userWalletStatus\n" +
                            "hasLocalWallet=$hasLocalWallet\n" +
                            "auto-creating new spark wallet"
                    }
                    ensureWalletAndConnect(userId, register = true)
                }
            }
        } catch (e: CancellationException) {
            Napier.d { "Wallet initialization cancelled for userId=$userId" }
            throw e
        }
    }

    private suspend fun ensureWalletAndConnect(userId: String, register: Boolean) {
        suspend {
            ensureSparkWalletExistsUseCase.invoke(userId, register = register)
        }.retryOnFailureWithAbort(
            times = MAX_RETRY_ATTEMPTS,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { _, remainingAttempts, delaySeconds, error ->
                Napier.w(throwable = error) {
                    "ensureWalletAndConnect failed for userId=$userId, " +
                        "retrying in ${delaySeconds}s ($remainingAttempts attempts left)"
                }
            },
        ).onFailure { error ->
            Napier.e(throwable = error) { "ensureWalletAndConnect failed for userId=$userId" }
        }.onSuccess { walletId ->
            currentWalletId = walletId
            Napier.d { "Wallet connected for userId=$userId, walletId=$walletId" }
            migrateTransactions(userId = userId, walletId = walletId)
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
