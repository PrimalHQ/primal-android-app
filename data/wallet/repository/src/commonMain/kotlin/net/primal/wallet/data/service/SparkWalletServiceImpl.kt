package net.primal.wallet.data.service

import breez_sdk_spark.GetInfoRequest
import breez_sdk_spark.InputType
import breez_sdk_spark.ListPaymentsRequest
import breez_sdk_spark.ListUnclaimedDepositsRequest
import breez_sdk_spark.LnurlPayRequest
import breez_sdk_spark.OnchainConfirmationSpeed
import breez_sdk_spark.Payment
import breez_sdk_spark.PaymentDetails
import breez_sdk_spark.PaymentType
import breez_sdk_spark.PrepareLnurlPayRequest
import breez_sdk_spark.PrepareSendPaymentRequest
import breez_sdk_spark.ReceivePaymentMethod
import breez_sdk_spark.ReceivePaymentRequest
import breez_sdk_spark.SdkEvent
import breez_sdk_spark.SdkException
import breez_sdk_spark.SendPaymentMethod
import breez_sdk_spark.SendPaymentOptions
import breez_sdk_spark.SendPaymentRequest
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withTimeout
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.Result
import net.primal.core.utils.mapFailure
import net.primal.core.utils.runCatching
import net.primal.domain.events.EventRepository
import net.primal.domain.rates.fees.OnChainTransactionFeeTier
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.LightningPaymentResult
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.PayResult
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.TransactionsPage
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.exception.WalletException
import net.primal.domain.wallet.exception.WalletFeesException
import net.primal.domain.wallet.exception.WalletPaymentException
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.repository.mappers.remote.mapAsSparkTransaction
import net.primal.wallet.data.spark.BreezSdkInstanceManager

internal class SparkWalletServiceImpl(
    private val breezSdkInstanceManager: BreezSdkInstanceManager,
    private val eventRepository: EventRepository,
    private val sparkWalletManager: SparkWalletManager,
    private val sparkSdkEventProvider: SparkSdkEventProvider,
) : WalletService<Wallet.Spark> {

    private companion object Companion {
        private const val DEFAULT_OFFSET = 0u
        private const val DEFAULT_LIMIT = 100u
        private const val DEFAULT_INVOICE_EXPIRY_SECS = 3600u
        private const val DEFAULT_COMPLETION_TIMEOUT_SECS = 30u
        private const val MAX_SAME_TIMESTAMP_PAGES = 10

        private const val TIER_SLOW_ID = "slow"
        private const val TIER_MEDIUM_ID = "medium"
        private const val TIER_FAST_ID = "fast"
    }

    override suspend fun fetchWalletBalance(wallet: Wallet.Spark): Result<WalletBalanceResult> =
        runCatching {
            val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)
            val info = sdk.getInfo(GetInfoRequest(false))
            WalletBalanceResult(
                balanceInBtc = info.balanceSats.toLong().toBtc(),
                maxBalanceInBtc = null,
            )
        }.mapFailure { it.toWalletException() }

    override suspend fun subscribeToWalletBalance(wallet: Wallet.Spark): Flow<WalletBalanceResult> {
        return flow {
            fetchWalletBalance(wallet).getOrNull()?.let { emit(it) }

            sparkWalletManager.balanceChanged
                .filter { it == wallet.walletId }
                .collect {
                    fetchWalletBalance(wallet).getOrNull()?.let { emit(it) }
                }
        }
    }

    override suspend fun fetchTransactions(
        wallet: Wallet.Spark,
        request: TransactionsRequest,
    ): Result<TransactionsPage> =
        runCatching {
            val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)
            val limit = request.limit ?: DEFAULT_LIMIT.toInt()

            val firstBatch = fetchClaimedTransactions(
                wallet = wallet,
                until = request.until,
                since = request.since,
                offset = request.offset ?: 0,
                limit = limit,
            )

            val allClaimed: List<Transaction>
            val nextCursor: Long?

            if (firstBatch.size < limit) {
                allClaimed = firstBatch
                nextCursor = null
            } else {
                val minCreatedAt = firstBatch.minOfOrNull { it.createdAt }!!
                val allSameTimestamp = firstBatch.all { it.createdAt == minCreatedAt }

                if (allSameTimestamp) {
                    // Same-timestamp edge case: use offset to exhaust this timestamp
                    val accumulated = firstBatch.toMutableList()
                    var offset = limit
                    while (offset < limit * MAX_SAME_TIMESTAMP_PAGES) {
                        val innerBatch = fetchClaimedTransactions(
                            wallet = wallet,
                            // Clamp to exact timestamp: toTimestamp is EXCLUSIVE in Breez SDK
                            until = minCreatedAt + 1,
                            since = minCreatedAt,
                            offset = offset,
                            limit = limit,
                        )
                        accumulated.addAll(innerBatch)
                        offset += limit
                        if (innerBatch.size < limit) break
                    }
                    allClaimed = accumulated
                    // toTimestamp is exclusive, so until=T will fetch < T on next page
                    nextCursor = minCreatedAt
                } else {
                    allClaimed = firstBatch
                    nextCursor = minCreatedAt
                }
            }

            // Merge unclaimed deposits as PROCESSING transactions.
            // Dedup by txid — prefer claimed version if both exist.
            val claimedTxIds = allClaimed
                .filterIsInstance<Transaction.OnChain>()
                .mapTo(mutableSetOf()) { it.onChainTxId }

            val unclaimedDeposits = try {
                sdk.listUnclaimedDeposits(ListUnclaimedDepositsRequest).deposits
            } catch (e: Exception) {
                Napier.w { "Failed to list unclaimed deposits: ${e.message}" }
                emptyList()
            }

            val unclaimedTransactions = unclaimedDeposits
                .filter { it.txid !in claimedTxIds }
                .map { it.mapAsSparkTransaction(wallet) }

            TransactionsPage(
                transactions = allClaimed + unclaimedTransactions,
                nextCursor = nextCursor,
            )
        }.mapFailure { it.toWalletException() }

    private suspend fun fetchClaimedTransactions(
        wallet: Wallet.Spark,
        until: Long?,
        since: Long?,
        offset: Int,
        limit: Int,
    ): List<Transaction> {
        val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)
        val response = sdk.listPayments(
            ListPaymentsRequest(
                offset = offset.toUInt(),
                limit = limit.toUInt(),
                sortAscending = false,
                fromTimestamp = since?.toULong(),
                toTimestamp = until?.toULong(),
            ),
        )

        val payments = response.payments

        val sentLightningInvoices = payments.extractSentInvoices()
        val zapReceiptsMap = if (sentLightningInvoices.isNotEmpty()) {
            eventRepository.getZapReceipts(invoices = sentLightningInvoices).getOrNull()
        } else {
            null
        }

        return payments.mapNotNull { payment ->
            val txInvoice = payment.extractSentInvoice()
            val zapRequestFallback = txInvoice?.let { zapReceiptsMap?.get(it) }

            payment.mapAsSparkTransaction(
                userId = wallet.userId,
                walletId = wallet.walletId,
                walletAddress = wallet.lightningAddress,
                zapRequestFallback = zapRequestFallback,
            )
        }
    }

    private fun List<Payment>.extractSentInvoices(): List<String> {
        return this
            .filter { it.paymentType == PaymentType.SEND }
            .mapNotNull { it.extractSentInvoice() }
    }

    private fun Payment.extractSentInvoice(): String? {
        if (this.paymentType != PaymentType.SEND) return null
        return when (val details = this.details) {
            is PaymentDetails.Lightning -> details.invoice
            is PaymentDetails.Spark -> details.invoiceDetails?.invoice
            else -> null
        }
    }

    private fun Payment.extractPreimage(): String? =
        when (val details = this.details) {
            is PaymentDetails.Lightning -> details.preimage
            is PaymentDetails.Spark -> details.htlcDetails?.preimage
            else -> null
        }

    override suspend fun createLightningInvoice(
        wallet: Wallet.Spark,
        request: LnInvoiceCreateRequest,
    ): Result<LnInvoiceCreateResult> =
        runCatching {
            val amountInBtc = request.amountInBtc
            requireNotNull(amountInBtc) { "Amount is required for Spark lightning invoices." }

            val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)
            val amountSats = (amountInBtc.toDouble() * 100_000_000).toLong()

            val response = sdk.receivePayment(
                ReceivePaymentRequest(
                    paymentMethod = ReceivePaymentMethod.Bolt11Invoice(
                        description = request.description ?: "",
                        amountSats = amountSats.toULong(),
                        expirySecs = DEFAULT_INVOICE_EXPIRY_SECS,
                    ),
                ),
            )

            LnInvoiceCreateResult(
                invoice = response.paymentRequest,
                description = request.description,
            )
        }.mapFailure { it.toWalletException() }

    override suspend fun createOnChainAddress(wallet: Wallet.Spark): Result<OnChainAddressResult> =
        runCatching {
            val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)
            val response = sdk.receivePayment(
                ReceivePaymentRequest(
                    paymentMethod = ReceivePaymentMethod.BitcoinAddress,
                ),
            )

            OnChainAddressResult(address = response.paymentRequest)
        }.mapFailure { it.toWalletException() }

    private fun parseAmountSats(amountSats: String): BigInteger {
        return try {
            BigInteger.parseString(amountSats)
        } catch (e: NumberFormatException) {
            throw InvalidAmountException(amountSats, e)
        }
    }

    override suspend fun pay(wallet: Wallet.Spark, request: TxRequest): Result<PayResult> =
        runCatching {
            val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)

            val payment = when (request) {
                is TxRequest.BitcoinOnChain -> {
                    val amountSats = parseAmountSats(request.amountSats)
                    val prepareResponse = sdk.prepareSendPayment(
                        PrepareSendPaymentRequest(
                            paymentRequest = request.onChainAddress,
                            amount = amountSats,
                        ),
                    )

                    val paymentMethod = prepareResponse.paymentMethod
                    if (paymentMethod is SendPaymentMethod.BitcoinAddress) {
                        val nowTimestamp = Clock.System.now().epochSeconds.toULong()
                        if (paymentMethod.feeQuote.expiresAt <= nowTimestamp) {
                            throw FeeQuoteExpiredException()
                        }
                    }

                    val confirmationSpeed = when (request.onChainTierId) {
                        TIER_SLOW_ID -> OnchainConfirmationSpeed.SLOW
                        TIER_MEDIUM_ID -> OnchainConfirmationSpeed.MEDIUM
                        TIER_FAST_ID -> OnchainConfirmationSpeed.FAST
                        else -> {
                            Napier.w { "Unknown fee tier '${request.onChainTierId}', defaulting to MEDIUM" }
                            OnchainConfirmationSpeed.MEDIUM
                        }
                    }

                    sdk.sendPayment(
                        SendPaymentRequest(
                            prepareResponse = prepareResponse,
                            options = SendPaymentOptions.BitcoinAddress(
                                confirmationSpeed = confirmationSpeed,
                            ),
                            idempotencyKey = request.idempotencyKey,
                        ),
                    ).payment
                }

                is TxRequest.Lightning.LnInvoice -> {
                    val prepareRequest = PrepareSendPaymentRequest(paymentRequest = request.lnInvoice)
                    val prepareResponse = sdk.prepareSendPayment(prepareRequest)
                    sdk.sendPayment(
                        SendPaymentRequest(
                            prepareResponse = prepareResponse,
                            options = SendPaymentOptions.Bolt11Invoice(
                                preferSpark = true,
                                completionTimeoutSecs = DEFAULT_COMPLETION_TIMEOUT_SECS,
                            ),
                            idempotencyKey = request.idempotencyKey,
                        ),
                    ).payment
                }

                is TxRequest.Lightning.LnUrl -> {
                    val payRequest = when (val parsedInput = sdk.parse(request.lnUrl)) {
                        is InputType.LnurlPay -> parsedInput.v1
                        is InputType.LightningAddress -> parsedInput.v1.payRequest
                        else -> error("Invalid LNURL-pay input: expected LnurlPay or LightningAddress")
                    }

                    val prepareResponse = sdk.prepareLnurlPay(
                        PrepareLnurlPayRequest(
                            amountSats = request.amountSats.toULong(),
                            payRequest = payRequest,
                            comment = request.noteRecipient,
                            validateSuccessActionUrl = true,
                        ),
                    )

                    sdk.lnurlPay(
                        LnurlPayRequest(
                            prepareResponse = prepareResponse,
                            idempotencyKey = request.idempotencyKey,
                        ),
                    ).payment
                }
            }

            val preimage = payment.extractPreimage()
            val feesPaid = runCatching { payment.fees.longValue() }.getOrNull()

            PayResult(preimage = preimage, feesPaid = feesPaid)
        }.mapFailure { it.toWalletException() }

    override suspend fun fetchMiningFees(
        wallet: Wallet.Spark,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<List<OnChainTransactionFeeTier>> =
        runCatching {
            val sdk = breezSdkInstanceManager.requireInstance(wallet.walletId)

            val amountSats = amountInBtc.toSats()
            val prepareRequest = PrepareSendPaymentRequest(
                paymentRequest = onChainAddress,
                amount = amountSats.toBigInteger(),
            )

            val prepareResponse = sdk.prepareSendPayment(prepareRequest)
            val paymentMethod = prepareResponse.paymentMethod

            if (paymentMethod !is SendPaymentMethod.BitcoinAddress) {
                return Result.failure(IllegalArgumentException("Mining fees are only for on-chain payments"))
            }

            val feeQuote = paymentMethod.feeQuote
            val expiresAt = feeQuote.expiresAt.toLong()

            listOf(
                OnChainTransactionFeeTier(
                    tierId = TIER_SLOW_ID,
                    label = "Slow",
                    confirmationEstimationInMin = 24.hours.inWholeMinutes.toInt(),
                    txFeeInBtc = (feeQuote.speedSlow.userFeeSat + feeQuote.speedSlow.l1BroadcastFeeSat)
                        .toLong().toBtc().toString(),
                    expiresAt = expiresAt,
                ),
                OnChainTransactionFeeTier(
                    tierId = TIER_MEDIUM_ID,
                    label = "Medium",
                    confirmationEstimationInMin = 60,
                    txFeeInBtc = (feeQuote.speedMedium.userFeeSat + feeQuote.speedMedium.l1BroadcastFeeSat)
                        .toLong().toBtc().toString(),
                    expiresAt = expiresAt,
                ),
                OnChainTransactionFeeTier(
                    tierId = TIER_FAST_ID,
                    label = "Fast",
                    confirmationEstimationInMin = 10,
                    txFeeInBtc = (feeQuote.speedFast.userFeeSat + feeQuote.speedFast.l1BroadcastFeeSat)
                        .toLong().toBtc().toString(),
                    expiresAt = expiresAt,
                ),
            )
        }.mapFailure { it.toWalletException() }

    override suspend fun awaitLightningPayment(
        wallet: Wallet.Spark,
        invoice: String?,
        timeout: Duration,
    ): Result<LightningPaymentResult> =
        runCatching {
            Napier.d { "Awaiting lightning payment (invoice=${invoice?.take(30)}, timeout=$timeout)" }
            withTimeout(timeout) {
                val event = sparkSdkEventProvider.sdkEvents
                    .filter { it.walletId == wallet.walletId }
                    .mapNotNull { (it.event as? SdkEvent.PaymentSucceeded)?.payment }
                    .first { payment ->
                        if (invoice != null) payment.matchesInvoice(invoice) else payment.isNonZapReceive()
                    }
                Napier.i { "Payment confirmed via event! paymentId=${event.id}" }
                LightningPaymentResult(amountInBtc = event.amount.longValue().toBtc().toString())
            }
        }.mapFailure { it.toWalletException() }

    private fun Payment.matchesInvoice(invoice: String): Boolean {
        val matched = when (val details = this.details) {
            is PaymentDetails.Lightning -> details.invoice
            is PaymentDetails.Spark -> details.invoiceDetails?.invoice
            else -> null
        }
        return matched == invoice
    }

    private fun Payment.isNonZapReceive(): Boolean {
        if (paymentType != PaymentType.RECEIVE) return false
        return when (val details = this.details) {
            is PaymentDetails.Lightning ->
                details.lnurlReceiveMetadata?.nostrZapRequest == null &&
                    details.lnurlReceiveMetadata?.nostrZapReceipt == null

            is PaymentDetails.Spark -> true
            else -> false
        }
    }

    private fun Throwable.toWalletException(): WalletException {
        return when (this) {
            is WalletException -> this
            is SdkException.NetworkException -> WalletException.WalletNetworkException(cause = this)
            is SdkException.InvalidInput -> WalletPaymentException.InvalidPaymentRequest(
                reason = this.v1,
                cause = this,
            )
            // Note: SdkException.InsufficientBalance handled via SparkException message check below
            is SdkException.LnurlException -> WalletPaymentException.PaymentFailed(
                reason = this.v1,
                cause = this,
            )

            is SdkException.SparkException -> {
                val reason = this.v1
                // Check for insufficient balance message in SparkException
                if (reason.contains("insufficient", ignoreCase = true) &&
                    (
                        reason.contains("balance", ignoreCase = true) ||
                            reason.contains("funds", ignoreCase = true)
                        )
                ) {
                    WalletPaymentException.InsufficientBalance(cause = this)
                } else {
                    WalletPaymentException.PaymentFailed(reason = reason, cause = this)
                }
            }

            is SdkException.Generic -> WalletPaymentException.PaymentFailed(
                reason = this.v1,
                cause = this,
            )

            is FeeQuoteExpiredException -> WalletFeesException.FeeQuoteExpired(cause = this)

            is InvalidAmountException -> WalletPaymentException.InvalidPaymentRequest(
                reason = this.message ?: "Invalid amount format",
                cause = this,
            )

            is IllegalArgumentException -> {
                val msg = this.message ?: "Unknown error"
                WalletPaymentException.InvalidPaymentRequest(reason = msg, cause = this)
            }

            else -> WalletPaymentException.PaymentFailed(
                reason = this.message ?: "Unknown error",
                cause = this,
            )
        }
    }

    /**
     * Custom exception for fee quote expiry to avoid fragile string matching.
     */
    private class FeeQuoteExpiredException :
        Exception("Fee quote has expired. Please refresh and try again.")

    /**
     * Custom exception for invalid amount format.
     */
    private class InvalidAmountException(amount: String, cause: Throwable) :
        Exception("Invalid amount format: '$amount'. Expected a valid number.", cause)
}
