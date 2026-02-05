package net.primal.wallet.data.repository.handler

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.aakira.napier.Napier
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
    }

    private val migrationMutex = Mutex()
    private var currentStep: MigrationStep = MigrationStep.CREATING_WALLET

    suspend fun invoke(userId: String, onProgress: (MigrationProgress) -> Unit = {}): Result<Unit> =
        migrationMutex.withLock {
            currentStep = MigrationStep.CREATING_WALLET
            var registeredWalletId: String? = null
            var fundsTransferred = false

            withContext(dispatcherProvider.io()) {
                runCatching {
                    Napier.i { "Starting Primal→Spark migration for user $userId" }

                    val sparkWalletId = createSparkWallet(userId = userId, onProgress = onProgress)
                    registerSparkWallet(userId = userId, sparkWalletId = sparkWalletId, onProgress = onProgress)
                    registeredWalletId = sparkWalletId

                    val balanceInBtc = checkBalance(userId = userId, onProgress = onProgress)
                    if (balanceInBtc.isPositiveBtcAmount()) {
                        val invoice = createInvoice(
                            sparkWalletId = sparkWalletId,
                            balanceInBtc = balanceInBtc,
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
                        Napier.d { "No balance to transfer, skipping balance transfer." }
                    }

                    finalizeWallet(userId = userId, sparkWalletId = sparkWalletId, onProgress = onProgress)

                    importTransactionHistory(
                        userId = userId,
                        sparkWalletId = sparkWalletId,
                        onProgress = onProgress,
                    )

                    onProgress(MigrationProgress.Completed)
                    Napier.i { "Migration completed successfully for user $userId" }
                }.onFailure { error ->
                    Napier.e(throwable = error) { "Migration failed at step $currentStep" }

                    // Rollback if wallet was registered but funds weren't transferred yet
                    val walletId = registeredWalletId
                    if (walletId != null && !fundsTransferred) {
                        rollbackRegistration(userId = userId, sparkWalletId = walletId)
                    }

                    onProgress(MigrationProgress.Failed(currentStep, error))
                }
            }
        }

    private suspend fun createSparkWallet(userId: String, onProgress: (MigrationProgress) -> Unit): String {
        currentStep = MigrationStep.CREATING_WALLET
        onProgress(MigrationProgress.InProgress(currentStep))
        Napier.d { "Step: Creating Spark wallet locally" }

        return suspend {
            ensureSparkWalletExistsUseCase.invoke(userId = userId, register = false)
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                Napier.w { "EnsureSparkWallet failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
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
        Napier.d { "Step: Registering Spark wallet" }

        suspend {
            sparkWalletAccountRepository.registerSparkWallet(
                userId = userId,
                walletId = sparkWalletId,
            )
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                Napier.w { "RegisterSparkWallet failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
            },
        ).getOrThrow()
    }

    private suspend fun checkBalance(userId: String, onProgress: (MigrationProgress) -> Unit): String {
        currentStep = MigrationStep.CHECKING_BALANCE
        onProgress(MigrationProgress.InProgress(currentStep))
        Napier.d { "Step: Checking Primal balance" }

        val balance = suspend {
            runCatching { primalWalletApi.getBalance(userId) }
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                Napier.w { "GetBalance failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
            },
        ).getOrThrow()

        Napier.d { "Primal balance: ${balance.amount}" }
        return balance.amount
    }

    private suspend fun createInvoice(
        sparkWalletId: String,
        balanceInBtc: String,
        onProgress: (MigrationProgress) -> Unit,
    ): String {
        currentStep = MigrationStep.CREATING_INVOICE
        onProgress(MigrationProgress.InProgress(currentStep))
        Napier.d { "Step: Creating invoice on Spark wallet for $balanceInBtc BTC" }

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
                Napier.w { "CreateInvoice failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
            },
        ).getOrThrow().invoice

        // Verify invoice amount matches expected balance
        val expectedSats = balanceInBtc.toSats()
        val invoiceSats = LnInvoiceUtils.getAmountInSatsOrNull(invoice)?.toULong() ?: 0L
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
        Napier.d { "Step: Transferring funds from Primal to Spark" }

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
                Napier.w { "Withdraw failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
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
        Napier.d { "Step: Awaiting payment confirmation in Spark wallet" }

        walletRepository.awaitInvoicePayment(
            walletId = sparkWalletId,
            invoice = invoice,
            timeout = PAYMENT_CONFIRMATION_TIMEOUT,
        ).onFailure { confirmationError ->
            Napier.e { "Payment confirmation failed: ${confirmationError.message}" }

            // Check Spark wallet balance directly - if > 0, funds arrived
            walletRepository.fetchWalletBalance(walletId = sparkWalletId)
            val wallet = walletRepository.getWalletById(walletId = sparkWalletId).getOrNull()
            val balance = wallet?.balanceInBtc ?: 0.0
            if (balance > 0.0) {
                Napier.w { "Confirmation failed but Spark balance is $balance BTC. Continuing." }
            } else {
                throw confirmationError
            }
        }

        Napier.i { "Payment confirmed successfully in Spark wallet" }
    }

    private suspend fun rollbackRegistration(userId: String, sparkWalletId: String) {
        Napier.w { "Rolling back Spark wallet registration for walletId=$sparkWalletId" }
        suspend {
            sparkWalletAccountRepository.unregisterSparkWallet(userId, sparkWalletId)
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                Napier.w { "UnregisterSparkWallet failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
            },
        ).onFailure { error ->
            Napier.e { "Failed to rollback registration after all retries: ${error.message}" }
        }
    }

    private suspend fun finalizeWallet(
        userId: String,
        sparkWalletId: String,
        onProgress: (MigrationProgress) -> Unit,
    ) {
        currentStep = MigrationStep.FINALIZING_WALLET
        onProgress(MigrationProgress.InProgress(currentStep))
        Napier.d { "Step: Finalizing wallet setup" }

        // Fetch wallet info (lightning address) with retry
        suspend {
            sparkWalletAccountRepository.fetchWalletAccountInfo(userId, sparkWalletId)
        }.retryOnFailureWithAbort(
            times = MAX_RETRIES,
            initialDelaySeconds = INITIAL_RETRY_DELAY_SECONDS,
            onRetry = { attempt, _, delay, error ->
                Napier.w { "FetchWalletAccountInfo failed (attempt $attempt, retry in ${delay}s): ${error.message}" }
            },
        ).onFailure { error ->
            Napier.w { "Failed to fetch wallet info after all retries: ${error.message}" }
        }

        // Delete old Primal wallet (best-effort, no retry)
        runCatching {
            walletRepository.deleteWalletById(walletId = userId)
        }.onFailure { error ->
            Napier.e(throwable = error) { "Failed to delete Primal wallet." }
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
        Napier.d { "Step: Importing transaction history" }

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
            Napier.w { "Transaction migration incomplete, will retry in background: ${error.message}" }
        }.onSuccess {
            Napier.d { "Transaction migration completed successfully" }
        }
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
