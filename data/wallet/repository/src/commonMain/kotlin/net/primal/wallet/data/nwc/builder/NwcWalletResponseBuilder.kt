package net.primal.wallet.data.nwc.builder

import net.primal.core.networking.nwc.nip47.GetBalanceResponsePayload
import net.primal.core.networking.nwc.nip47.GetInfoResponsePayload
import net.primal.core.networking.nwc.nip47.ListTransactionsResponsePayload
import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.MakeInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.nip47.NwcResponseContent
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.utils.serialization.CommonJson

class NwcWalletResponseBuilder {

    fun buildGetInfoResponse(request: WalletNwcRequest, result: GetInfoResponsePayload): String {
        val response = NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
        return CommonJson.encodeToString(response)
    }

    fun buildGetBalanceResponse(request: WalletNwcRequest, result: GetBalanceResponsePayload): String {
        val response = NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
        return CommonJson.encodeToString(response)
    }

    fun buildPayInvoiceResponse(request: WalletNwcRequest, result: PayInvoiceResponsePayload): String {
        val response = NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
        return CommonJson.encodeToString(response)
    }

    fun buildListTransactionsResponse(request: WalletNwcRequest, result: ListTransactionsResponsePayload): String {
        val response = NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
        return CommonJson.encodeToString(response)
    }

    fun buildMakeInvoiceResponse(request: WalletNwcRequest, result: MakeInvoiceResponsePayload): String {
        val response = NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
        return CommonJson.encodeToString(response)
    }

    fun buildLookupInvoiceResponse(request: WalletNwcRequest, result: LookupInvoiceResponsePayload): String {
        val response = NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
        return CommonJson.encodeToString(response)
    }

    fun buildErrorResponse(
        request: WalletNwcRequest,
        code: String,
        message: String,
    ): String {
        val response = NwcResponseContent<Unit>(
            resultType = request.methodStr(),
            error = NwcError(code = code, message = message),
        )
        return CommonJson.encodeToString(response)
    }

    fun buildParsingErrorResponse(code: String, message: String): String {
        val response = NwcResponseContent<Unit>(
            resultType = "",
            error = NwcError(code = code, message = message),
        )
        return CommonJson.encodeToString(response)
    }

    private fun WalletNwcRequest.methodStr(): String {
        return when (this) {
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
    }
}
