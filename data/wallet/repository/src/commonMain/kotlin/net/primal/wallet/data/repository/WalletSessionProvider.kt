package net.primal.wallet.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
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

    private var currentSparkWalletId: String? = null

    fun start() {
        scope.launch {
            activeUserId
                .collectLatest { userIdOrNull ->
                    disconnectCurrentSparkWallet()
                    val userId = userIdOrNull ?: return@collectLatest
                    coroutineScope {
                        launch { provisionWalletOnServer(userId) }
                        launch { observeActiveWalletAndManageSparkSdk(userId) }
                    }
                }
        }
    }

    fun setActiveUserId(userId: String?) {
        activeUserId.value = userId?.takeIf { it.isNotBlank() }
    }

    private suspend fun disconnectCurrentSparkWallet() {
        currentSparkWalletId?.let { walletId ->
            sparkWalletManager.disconnectWallet(walletId)
                .onFailure { t ->
                    Napier.e(throwable = t) { "disconnectWallet failed for walletId=$walletId" }
                }
            currentSparkWalletId = null
        }
    }

    private suspend fun observeActiveWalletAndManageSparkSdk(userId: String) {
        walletAccountRepository.observeActiveWallet(userId)
            .collect { userWallet ->
                val newSparkWalletId = (userWallet?.wallet as? Wallet.Spark)?.walletId
                when {
                    newSparkWalletId == currentSparkWalletId -> Unit
                    newSparkWalletId != null -> {
                        if (connectSparkWallet(newSparkWalletId)) {
                            disconnectCurrentSparkWallet()
                            currentSparkWalletId = newSparkWalletId
                        }
                    }
                    else -> disconnectCurrentSparkWallet()
                }
            }
    }

    private suspend fun provisionWalletOnServer(userId: String) {
        try {
            val userWalletStatus = primalWalletAccountRepository.fetchWalletStatus(userId).getOrNull()
            val hasLocalWallet = sparkWalletAccountRepository.hasPersistedSparkWallet(userId)

            when {
                hasLocalWallet -> {
                    val shouldRegister = userWalletStatus?.isEligibleToRegisterSparkWallet == true
                    ensureSparkWalletExistsAndConnected(userId = userId, register = shouldRegister)
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
                    ensureSparkWalletExistsAndConnected(userId = userId, register = true)
                }
            }
        } catch (e: CancellationException) {
            Napier.d { "Wallet provisioning cancelled for userId=$userId" }
            throw e
        }
    }

    private suspend fun ensureSparkWalletExistsAndConnected(userId: String, register: Boolean) {
        suspend {
            ensureSparkWalletExistsUseCase.invoke(userId = userId, register = register)
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
            Napier.d { "Wallet provisioned for userId=$userId, walletId=$walletId" }
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

    private suspend fun connectSparkWallet(walletId: String): Boolean {
        if (sparkWalletManager.hasInstance(walletId)) return true

        val seedWords = sparkWalletAccountRepository
            .getPersistedSeedWords(walletId)
            .getOrNull()
            ?.joinToString(separator = " ")

        if (seedWords == null) {
            Napier.w { "connectSparkWallet: no seed words for walletId=$walletId" }
            return false
        }

        return sparkWalletManager.initializeWallet(seedWords)
            .onFailure { error ->
                Napier.e(throwable = error) { "connectSparkWallet failed for walletId=$walletId" }
            }
            .isSuccess
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 5
        const val INITIAL_RETRY_DELAY_SECONDS = 3
    }
}
