package net.primal.wallet.data.nwc.builder

import net.primal.core.networking.nwc.nip47.NwcError
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.nip47.NwcResponseContent
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest

class NwcWalletResponseBuilder {

    fun buildSuccessResponse(request: WalletNwcRequest, result: Any?): NwcResponseContent<Any?> {
        return NwcResponseContent(
            resultType = request.methodStr(),
            result = result,
        )
    }

    fun buildErrorResponse(
        request: WalletNwcRequest,
        code: String,
        message: String,
    ): NwcResponseContent<Any?> {
        return NwcResponseContent(
            resultType = request.methodStr(),
            error = NwcError(code = code, message = message),
        )
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
