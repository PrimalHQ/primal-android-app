package net.primal.wallet.data.repository.mappers.remote

import breez_sdk_spark.DepositInfo
import breez_sdk_spark.Payment
import breez_sdk_spark.PaymentDetails
import breez_sdk_spark.PaymentStatus
import breez_sdk_spark.PaymentType
import kotlin.time.Clock
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletType

/**
 * Maps a Spark Payment to a domain Transaction.
 * Returns null for Token transactions which are not supported.
 *
 * @param zapRequestFallback Optional zap request from external source (e.g., eventRepository).
 *                           Used as fallback when SDK doesn't provide zap data (e.g., for sent zaps).
 */
internal fun Payment.mapAsSparkTransaction(
    userId: String,
    walletId: String,
    walletAddress: String?,
    zapRequestFallback: NostrEvent?,
): Transaction? {
    val details = this.details

    // Skip Token transactions - not supported
    if (details is PaymentDetails.Token) return null

    val txType = when (this.paymentType) {
        PaymentType.RECEIVE -> TxType.DEPOSIT
        PaymentType.SEND -> TxType.WITHDRAW
    }

    val txState = when (this.status) {
        PaymentStatus.COMPLETED -> TxState.SUCCEEDED
        PaymentStatus.FAILED -> TxState.FAILED
        else -> TxState.PROCESSING
    }

    // Extract type-specific data from PaymentDetails
    val onChainTxId: String?
    val preimage: String?
    val paymentHash: String?
    val invoice: String?
    val otherLightningAddress: String?
    val sdkZapRequest: NostrEvent?
    val sdkDescription: String?

    when (details) {
        is PaymentDetails.Lightning -> {
            // Lightning payment - extract all available SDK data
            onChainTxId = null
            preimage = details.preimage
            paymentHash = details.paymentHash
            invoice = details.invoice

            // Extract LNURL metadata based on payment direction
            if (txType == TxType.DEPOSIT) {
                // Received payment - extract from lnurlReceiveMetadata
                val receiveMetadata = details.lnurlReceiveMetadata
                sdkZapRequest = receiveMetadata?.nostrZapRequest?.decodeFromJsonStringOrNull<NostrEvent>()
                sdkDescription = receiveMetadata?.senderComment
                    ?: details.description
                    ?: LnInvoiceUtils.getDescription(invoice)
                otherLightningAddress = null
            } else {
                // Sent payment - extract from lnurlPayInfo
                val payInfo = details.lnurlPayInfo
                sdkZapRequest = null // For sent zaps, we need external lookup
                sdkDescription = payInfo?.comment
                    ?: details.description
                    ?: LnInvoiceUtils.getDescription(invoice)
                otherLightningAddress = payInfo?.lnAddress
            }
        }
        is PaymentDetails.Deposit -> {
            // On-chain deposit or withdrawal
            onChainTxId = details.txId
            preimage = null
            paymentHash = null
            invoice = null
            otherLightningAddress = null
            sdkZapRequest = null
            sdkDescription = null
        }
        is PaymentDetails.Withdraw -> {
            // On-chain withdrawal
            onChainTxId = details.txId
            preimage = null
            paymentHash = null
            invoice = null
            otherLightningAddress = null
            sdkZapRequest = null
            sdkDescription = null
        }
        is PaymentDetails.Spark -> {
            // Spark-to-Spark transfer
            onChainTxId = null
            preimage = details.htlcDetails?.preimage
            paymentHash = details.htlcDetails?.paymentHash
            invoice = details.invoiceDetails?.invoice
            otherLightningAddress = null
            sdkZapRequest = null
            sdkDescription = details.invoiceDetails?.description
        }
        is PaymentDetails.Token, null -> {
            // Token transactions not supported, null details should not happen
            return null
        }
    }

    // Prefer SDK zap request, fallback to external source
    val zapRequest = sdkZapRequest ?: zapRequestFallback
    val zappedEntity = zapRequest?.toNostrEntity()
    val description = sdkDescription

    val timestampSeconds = this.timestamp.toLong()
    val completedAt = if (txState == TxState.SUCCEEDED) timestampSeconds else null
    val totalFeeInBtc = if (txType == TxType.WITHDRAW) {
        this.fees.longValue().toBtc().toString()
    } else {
        null
    }
    val otherUserId = when (txType) {
        TxType.DEPOSIT -> zapRequest?.pubKey
        TxType.WITHDRAW -> zapRequest?.tags?.findFirstProfileId()
    }

    // Create the appropriate domain transaction type based on payment details
    return when (details) {
        is PaymentDetails.Lightning -> {
            if (zappedEntity != null) {
                Transaction.Zap(
                    transactionId = this.id,
                    walletId = walletId,
                    walletType = WalletType.SPARK,
                    type = txType,
                    state = txState,
                    createdAt = timestampSeconds,
                    updatedAt = timestampSeconds,
                    completedAt = completedAt,
                    userId = userId,
                    note = description,
                    invoice = invoice,
                    amountInBtc = this.amount.longValue().toBtc(),
                    amountInUsd = null,
                    exchangeRate = null,
                    totalFeeInBtc = totalFeeInBtc,
                    zappedEntity = zappedEntity,
                    otherUserId = otherUserId,
                    otherLightningAddress = otherLightningAddress,
                    zappedByUserId = zapRequest?.pubKey,
                    otherUserProfile = null,
                    preimage = preimage,
                    paymentHash = paymentHash,
                )
            } else {
                Transaction.Lightning(
                    transactionId = this.id,
                    walletId = walletId,
                    walletType = WalletType.SPARK,
                    type = txType,
                    state = txState,
                    createdAt = timestampSeconds,
                    updatedAt = timestampSeconds,
                    completedAt = completedAt,
                    userId = userId,
                    note = description,
                    invoice = invoice,
                    amountInBtc = this.amount.longValue().toBtc(),
                    amountInUsd = null,
                    exchangeRate = null,
                    totalFeeInBtc = totalFeeInBtc,
                    otherUserId = otherUserId,
                    otherLightningAddress = otherLightningAddress,
                    otherUserProfile = null,
                    preimage = preimage,
                    paymentHash = paymentHash,
                )
            }
        }
        is PaymentDetails.Deposit, is PaymentDetails.Withdraw -> {
            Transaction.OnChain(
                transactionId = onChainTxId ?: error("onChainTxId should not be null here"),
                walletId = walletId,
                walletType = WalletType.SPARK,
                type = txType,
                state = txState,
                createdAt = timestampSeconds,
                updatedAt = timestampSeconds,
                completedAt = completedAt,
                userId = userId,
                note = description,
                invoice = invoice,
                amountInBtc = this.amount.longValue().toBtc(),
                amountInUsd = null,
                exchangeRate = null,
                totalFeeInBtc = totalFeeInBtc,
                onChainTxId = onChainTxId,
                onChainAddress = null,
            )
        }
        is PaymentDetails.Spark -> {
            if (zappedEntity != null) {
                Transaction.Zap(
                    transactionId = this.id,
                    walletId = walletId,
                    walletType = WalletType.SPARK,
                    type = txType,
                    state = txState,
                    createdAt = timestampSeconds,
                    updatedAt = timestampSeconds,
                    completedAt = completedAt,
                    userId = userId,
                    note = description,
                    invoice = invoice,
                    amountInBtc = this.amount.longValue().toBtc(),
                    amountInUsd = null,
                    exchangeRate = null,
                    totalFeeInBtc = totalFeeInBtc,
                    zappedEntity = zappedEntity,
                    otherUserId = otherUserId,
                    otherLightningAddress = null,
                    zappedByUserId = zapRequest?.pubKey,
                    otherUserProfile = null,
                    preimage = preimage,
                    paymentHash = paymentHash,
                )
            } else {
                Transaction.Spark(
                    transactionId = this.id,
                    walletId = walletId,
                    walletType = WalletType.SPARK,
                    type = txType,
                    state = txState,
                    createdAt = timestampSeconds,
                    updatedAt = timestampSeconds,
                    completedAt = completedAt,
                    userId = userId,
                    note = description,
                    invoice = invoice,
                    amountInBtc = this.amount.longValue().toBtc(),
                    amountInUsd = null,
                    exchangeRate = null,
                    totalFeeInBtc = totalFeeInBtc,
                    sparkAddress = null,
                    preimage = preimage,
                    paymentHash = paymentHash,
                )
            }
        }
        is PaymentDetails.Token -> null
    }
}

internal fun DepositInfo.mapAsSparkTransaction(wallet: Wallet.Spark): Transaction.OnChain {
    val now = Clock.System.now().epochSeconds
    return Transaction.OnChain(
        transactionId = this.txid,
        walletId = wallet.walletId,
        walletType = WalletType.SPARK,
        type = TxType.DEPOSIT,
        state = TxState.PROCESSING,
        createdAt = now,
        updatedAt = now,
        completedAt = null,
        userId = wallet.userId,
        note = null,
        invoice = null,
        amountInBtc = this.amountSats.toLong().toBtc(),
        amountInUsd = null,
        exchangeRate = null,
        totalFeeInBtc = null,
        onChainTxId = this.txid,
        onChainAddress = null,
    )
}
