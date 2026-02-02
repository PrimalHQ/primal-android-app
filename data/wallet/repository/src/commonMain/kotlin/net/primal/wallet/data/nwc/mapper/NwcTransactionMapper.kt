package net.primal.wallet.data.nwc.mapper

import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.utils.CurrencyConversionUtils.btcToMSats
import net.primal.domain.nostr.InvoiceType
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.wallet.data.repository.mappers.local.extractPaymentHash
import net.primal.wallet.data.repository.mappers.local.extractPreimage

fun Transaction.toNwcTransaction(): LookupInvoiceResponsePayload {
    val amountMsats = kotlin.math.abs(amountInBtc.btcToMSats().toLong())
    val feeMsats = kotlin.math.abs(totalFeeInBtc?.toDoubleOrNull()?.btcToMSats()?.toLong() ?: 0L)

    return LookupInvoiceResponsePayload(
        type = type.toInvoiceType(),
        invoice = invoice,
        description = note,
        amount = amountMsats,
        feesPaid = feeMsats,
        createdAt = createdAt,
        settledAt = completedAt,
        paymentHash = this.extractPaymentHash(),
        preimage = this.extractPreimage(),
        state = state.toNwcState(),
    )
}

fun TxType.toInvoiceType(): InvoiceType =
    when (this) {
        TxType.DEPOSIT -> InvoiceType.Incoming
        TxType.WITHDRAW -> InvoiceType.Outgoing
    }

fun InvoiceType.toTxType(): TxType =
    when (this) {
        InvoiceType.Incoming -> TxType.DEPOSIT
        InvoiceType.Outgoing -> TxType.WITHDRAW
    }

private fun TxState.toNwcState(): String =
    when (this) {
        TxState.SUCCEEDED -> "settled"
        TxState.FAILED, TxState.CANCELED -> "failed"
        TxState.CREATED, TxState.PROCESSING -> "pending"
    }
