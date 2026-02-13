package net.primal.wallet.data.nwc.mapper

import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.wallet.exception.WalletConnectionException
import net.primal.domain.wallet.exception.WalletInvoiceException
import net.primal.domain.wallet.exception.WalletPaymentException
import net.primal.domain.wallet.nwc.model.NwcRequestLog
import net.primal.domain.wallet.nwc.model.NwcRequestState
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.nwc.NwcWalletRequestLog

fun buildNwcRequestLog(request: WalletNwcRequest, requestedAt: Long): NwcWalletRequestLog {
    return NwcWalletRequestLog(
        eventId = request.eventId,
        connectionId = request.connection.secretPubKey,
        walletId = request.connection.walletId,
        userId = request.connection.userId,
        method = request.resolveMethodType().asEncryptable(),
        requestPayload = request.resolvePayloadJson().asEncryptable(),
        responsePayload = null,
        requestState = NwcRequestState.Processing.name.asEncryptable(),
        errorCode = null,
        errorMessage = null,
        requestedAt = requestedAt,
        completedAt = null,
        amountMsats = request.resolveAmountMsats()?.asEncryptable(),
    )
}

internal fun WalletNwcRequest.resolveMethodType(): String =
    when (this) {
        is WalletNwcRequest.PayInvoice -> NwcMethod.PayInvoice.value
        is WalletNwcRequest.PayKeysend -> NwcMethod.PayKeysend.value
        is WalletNwcRequest.MakeInvoice -> NwcMethod.MakeInvoice.value
        is WalletNwcRequest.LookupInvoice -> NwcMethod.LookupInvoice.value
        is WalletNwcRequest.ListTransactions -> NwcMethod.ListTransactions.value
        is WalletNwcRequest.GetBalance -> NwcMethod.GetBalance.value
        is WalletNwcRequest.GetInfo -> NwcMethod.GetInfo.value
        is WalletNwcRequest.MultiPayInvoice -> NwcMethod.MultiPayInvoice.value
        is WalletNwcRequest.MultiPayKeysend -> NwcMethod.MultiPayKeysend.value
    }

internal fun WalletNwcRequest.resolvePayloadJson(): String {
    return runCatching {
        when (this) {
            is WalletNwcRequest.PayInvoice -> params.encodeToJsonString()
            is WalletNwcRequest.MakeInvoice -> params.encodeToJsonString()
            is WalletNwcRequest.ListTransactions -> params.encodeToJsonString()
            is WalletNwcRequest.LookupInvoice -> params.encodeToJsonString()
            is WalletNwcRequest.PayKeysend -> params.encodeToJsonString()
            is WalletNwcRequest.MultiPayInvoice -> params.encodeToJsonString()
            is WalletNwcRequest.MultiPayKeysend -> params.encodeToJsonString()
            is WalletNwcRequest.GetBalance,
            is WalletNwcRequest.GetInfo,
            -> "{}"
        }
    }.getOrDefault("{}")
}

internal fun WalletNwcRequest.resolveAmountMsats(): Long? =
    when (this) {
        is WalletNwcRequest.PayInvoice -> params.amount
        is WalletNwcRequest.MakeInvoice -> params.amount
        is WalletNwcRequest.PayKeysend -> params.amount
        else -> null
    }

fun NwcWalletRequestLog.asDomain(): NwcRequestLog =
    NwcRequestLog(
        eventId = eventId,
        connectionId = connectionId,
        walletId = walletId,
        userId = userId,
        method = method.decrypted,
        requestPayload = requestPayload.decrypted,
        responsePayload = responsePayload?.decrypted,
        requestState = NwcRequestState.valueOf(requestState.decrypted),
        errorCode = errorCode?.decrypted,
        errorMessage = errorMessage?.decrypted,
        requestedAt = requestedAt,
        completedAt = completedAt,
        amountMsats = amountMsats?.decrypted,
    )

internal fun Throwable.resolveNwcErrorCode(): String =
    when (this) {
        is WalletPaymentException.InsufficientBalance -> NwcError.INSUFFICIENT_BALANCE
        is WalletPaymentException.OperationNotSupported -> NwcError.NOT_IMPLEMENTED
        is WalletPaymentException.InvalidPaymentRequest -> NwcError.OTHER
        is WalletInvoiceException.InvalidInvoiceAmount -> NwcError.OTHER
        is WalletConnectionException.RateLimited -> NwcError.RATE_LIMITED
        is WalletConnectionException.Unauthorized -> NwcError.UNAUTHORIZED
        is WalletConnectionException.QuotaExceeded -> NwcError.QUOTA_EXCEEDED
        else -> NwcError.INTERNAL
    }
