package net.primal.wallet.data.repository.handler

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.retryOnFailureWithAbort
import net.primal.core.utils.runCatching
import net.primal.core.utils.toULong
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.migration.MigrationProgress
import net.primal.domain.wallet.migration.MigrationStep
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.WithdrawRequestBody

class MigratePrimalToSparkWalletHandler(
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
    private val migratePrimalTransactionsHandler: MigratePrimalTransactionsHandler,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val walletRepository: WalletRepository,
    private val walletDatabase: WalletDatabase,
    private val primalWalletApi: PrimalWalletApi,
    private val dispatcherProvider: DispatcherProvider,
) {

    private companion object {
        private val PAYMENT_CONFIRMATION_TIMEOUT = 45.seconds
        private const val INITIAL_TRANSACTION_PAGES = 3
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_SECONDS = 1
        private const val LOG_TAG = "WalletMigration"

        private const val TEST_BUFFER_SATS = 100
    }

    private val migrationMutex = Mutex()
    private var currentStep: MigrationStep = MigrationStep.CREATING_WALLET
    private val migrationLogs = mutableListOf<String>()

    private fun logDebug(message: String) {
        val timestamp = Clock.System.now().toString()
        migrationLogs.add("[$timestamp] D: $message")
        Napier.d(tag = LOG_TAG) { message }
    }

    private fun logInfo(message: String) {
        val timestamp = Clock.System.now().toString()
        migrationLogs.add("[$timestamp] I: $message")
        Napier.i(tag = LOG_TAG) { message }
    }

    private fun logWarning(message: String) {
        val timestamp = Clock.System.now().toString()
        migrationLogs.add("[$timestamp] W: $message")
        Napier.w(tag = LOG_TAG) { message }
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        val timestamp = Clock.System.now().toString()
        val errorDetails = throwable?.let { ": ${it.stackTraceToString()}" } ?: ""
        migrationLogs.add("[$timestamp] E: $message$errorDetails")
        Napier.e(tag = LOG_TAG, throwable = throwable) { message }
    }

    suspend fun invoke(userId: String, onProgress: (MigrationProgress) -> Unit = {}): Result<Unit> =
        migrationMutex.withLock {
            migrationLogs.clear()
            currentStep = MigrationStep.CREATING_WALLET
            var registeredWalletId: String? = null
            var fundsTransferred = false

            withContext(dispatcherProvider.io()) {
                runCatching {
                    logInfo("Starting Primal→Spark migration for user $userId")

                    val sparkWalletId = createSparkWallet(userId = userId, onProgress = onProgress)
                    registerSparkWallet(userId = userId, sparkWalletId = sparkWalletId, onProgress = onProgress)
                    registeredWalletId = sparkWalletId

                    val balanceInBtc = checkBalance(userId = userId, onProgress = onProgress)
                    if (balanceInBtc.isPositiveBtcAmount()) {
                        val adjustedBalanceInBtc = balanceInBtc.subtractSats(TEST_BUFFER_SATS)
                        logDebug("Adjusted balance: $balanceInBtc BTC → $adjustedBalanceInBtc BTC (minus $TEST_BUFFER_SATS sats)")

                        val invoice = createInvoice(
                            sparkWalletId = sparkWalletId,
                            balanceInBtc = adjustedBalanceInBtc,
                            onProgress = onProgress,
                        )
                        transferBalance(userId = userId, invoice = invoice, onProgress = onProgress)

                        // Funds left Primal, no rollback from here
                        fundsTransferred = true
                        awaitPaymentConfirmation(
                            sparkWalletId = sparkWalletId,
                            invoice = invoice,
                            onProgress = onProgress,
                        )
                    } else {
                        logDebug("No balance to transfer, skipping balance transfer.")
                    }

                    finalizeWallet(userId = userId, sparkWalletId = sparkWalletId, onProgress = onProgress)

                    importTransactionHistory(
                        userId = userId,
                        sparkWalletId = sparkWalletId,
                        onProgress = onProgress,
                    )

                    onProgress(MigrationProgress.Completed)
                    logInfo("Migration completed successfully for user $userId")
                }.onFailure { error ->
                    logError("Migration failed at step $currentStep", error)

                    // Rollback if wallet was registered but funds weren't transferred yet
                    val walletId = registeredWalletId
                    if (walletId != null && !fundsTransferred) {
                        rollbackRegistration(userId = userId, sparkWalletId = walletId)
                    }

                    onProgress(MigrationProgress.Failed(currentStep, error, migrationLogs.toList()))
                }
            }
        }

    private suspend fun createSparkWallet(userId: String, onProgress: (MigrationProgress) -> Unit): String {
        currentStep = MigrationStep.CREATING_WALLET
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Creating Spark wallet locally")

        return suspend {
            ensureSparkWalletExistsUseCase.invoke(userId = userId, register = false)
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("EnsureSparkWallet failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).getOrThrow()
    }

    private suspend fun registerSparkWallet(
        userId: String,
        sparkWalletId: String,
        onProgress: (MigrationProgress) -> Unit,
    ) {
        currentStep = MigrationStep.REGISTERING_WALLET
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Registering Spark wallet")

        suspend {
            sparkWalletAccountRepository.registerSparkWallet(
                userId = userId,
                walletId = sparkWalletId,
            )
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("RegisterSparkWallet failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).getOrThrow()
    }

    private suspend fun checkBalance(userId: String, onProgress: (MigrationProgress) -> Unit): String {
        currentStep = MigrationStep.CHECKING_BALANCE
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Checking Primal balance")

        val balance = suspend {
            runCatching { primalWalletApi.getBalance(userId) }
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("GetBalance failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).getOrThrow()

        logDebug("Primal balance: ${balance.amount}")
        return balance.amount
    }

    private suspend fun createInvoice(
        sparkWalletId: String,
        balanceInBtc: String,
        onProgress: (MigrationProgress) -> Unit,
    ): String {
        currentStep = MigrationStep.CREATING_INVOICE
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Creating invoice on Spark wallet for $balanceInBtc BTC")

        val invoice = suspend {
            walletRepository.createLightningInvoice(
                walletId = sparkWalletId,
                amountInBtc = balanceInBtc,
                comment = "Receiving Wallet Balance",
            )
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("CreateInvoice failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).getOrThrow().invoice

        // Verify invoice amount matches expected balance
        val expectedSats = balanceInBtc.toSats()
        val invoiceSats = LnInvoiceUtils.getAmountInSatsOrNull(invoice)?.toULong() ?: 0UL
        if (invoiceSats != expectedSats) {
            error("Invoice amount mismatch: expected $expectedSats sats, got $invoiceSats sats")
        }

        return invoice
    }

    private suspend fun transferBalance(
        userId: String,
        invoice: String,
        onProgress: (MigrationProgress) -> Unit,
    ) {
        currentStep = MigrationStep.TRANSFERRING_FUNDS
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Transferring funds from Primal to Spark")

        suspend {
            runCatching {
                primalWalletApi.withdraw(
                    userId = userId,
                    body = WithdrawRequestBody(
                        subWallet = SubWallet.Open,
                        lnInvoice = invoice,
                        noteSelf = "Old Primal Wallet Balance",
                    ),
                )
            }
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("Withdraw failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).getOrThrow()
    }

    private suspend fun awaitPaymentConfirmation(
        sparkWalletId: String,
        invoice: String,
        onProgress: (MigrationProgress) -> Unit,
    ) {
        currentStep = MigrationStep.AWAITING_CONFIRMATION
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Awaiting payment confirmation in Spark wallet")

        walletRepository.awaitInvoicePayment(
            walletId = sparkWalletId,
            invoice = invoice,
            timeout = PAYMENT_CONFIRMATION_TIMEOUT,
        ).onFailure { confirmationError ->
            logError("Payment confirmation failed: ${confirmationError.message}")

            // Check Spark wallet balance directly - if > 0, funds arrived
            walletRepository.fetchWalletBalance(walletId = sparkWalletId)
            val wallet = walletRepository.getWalletById(walletId = sparkWalletId).getOrNull()
            val balance = wallet?.balanceInBtc ?: 0.0
            if (balance > 0.0) {
                logWarning("Confirmation failed but Spark balance is $balance BTC. Continuing.")
            } else {
                throw confirmationError
            }
        }

        logInfo("Payment confirmed successfully in Spark wallet")
    }

    private suspend fun rollbackRegistration(userId: String, sparkWalletId: String) {
        logWarning("Rolling back Spark wallet registration for walletId=$sparkWalletId")
        suspend {
            sparkWalletAccountRepository.unregisterSparkWallet(userId, sparkWalletId)
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("UnregisterSparkWallet failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).onFailure { error ->
            logError("Failed to rollback registration after all retries: ${error.message}")
        }
    }

    private suspend fun finalizeWallet(
        userId: String,
        sparkWalletId: String,
        onProgress: (MigrationProgress) -> Unit,
    ) {
        currentStep = MigrationStep.FINALIZING_WALLET
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Finalizing wallet setup")

        // Fetch wallet info (lightning address) with retry
        suspend {
            sparkWalletAccountRepository.fetchWalletAccountInfo(userId, sparkWalletId)
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                logWarning("FetchWalletAccountInfo failed (attempt $attempt, retry in ${delay}s): ${error.message}")
            },
        ).onFailure { error ->
            logWarning("Failed to fetch wallet info after all retries: ${error.message}")
        }

        // Delete old Primal wallet (best-effort, no retry)
        runCatching {
            walletRepository.deleteWalletById(walletId = userId)
        }.onFailure { error ->
            logError("Failed to delete Primal wallet.", error)
        }

        // Set Spark as active wallet
        walletAccountRepository.setActiveWallet(userId, sparkWalletId)
    }

    private suspend fun importTransactionHistory(
        userId: String,
        sparkWalletId: String,
        onProgress: (MigrationProgress) -> Unit,
    ) {
        currentStep = MigrationStep.IMPORTING_HISTORY
        onProgress(MigrationProgress.InProgress(currentStep))
        logDebug("Step: Importing transaction history")

        // Mark migration as started (false = needs more pages in background)
        walletDatabase.wallet().updatePrimalTxsMigrated(
            walletId = sparkWalletId,
            migrated = false,
        )

        migratePrimalTransactionsHandler.invoke(
            userId = userId,
            targetWalletId = sparkWalletId,
            maxPages = INITIAL_TRANSACTION_PAGES,
        ).onFailure { error ->
            logWarning("Transaction migration incomplete, will retry in background: ${error.message}")
        }.onSuccess {
            logDebug("Transaction migration completed successfully")
        }
    }

    /**
     * Subtracts the given number of sats from a BTC amount string.
     */
    private fun String.subtractSats(sats: Int): String {
        val btcAmount = BigDecimal.parseString(this)
        val satsInBtc = BigDecimal.fromInt(sats).divide(BigDecimal.fromLong(100_000_000L))
        return btcAmount.subtract(satsInBtc).toStringExpanded()
    }

    /**
     * Safely checks if a BTC amount string represents a positive value.
     * Uses BigDecimal for precise comparison to avoid floating point issues.
     */
    private fun String.isPositiveBtcAmount(): Boolean {
        if (this.isBlank()) return false
        return try {
            BigDecimal.parseString(this) > BigDecimal.ZERO
        } catch (_: Exception) {
            false
        }
    }
}
