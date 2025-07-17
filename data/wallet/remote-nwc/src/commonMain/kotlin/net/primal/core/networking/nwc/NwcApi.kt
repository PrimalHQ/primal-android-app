package net.primal.core.networking.nwc

import net.primal.core.networking.nwc.nip47.GetBalanceResponsePayload
import net.primal.core.networking.nwc.nip47.GetInfoResponsePayload
import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.ListTransactionsResponsePayload
import net.primal.core.networking.nwc.nip47.LookupInvoiceParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.MakeInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.PayKeysendParams
import net.primal.core.networking.nwc.nip47.PayKeysendResponsePayload
import net.primal.core.utils.Result

interface NwcApi {

    suspend fun getBalance(): Result<GetBalanceResponsePayload>

    suspend fun listTransactions(
        params: ListTransactionsParams = ListTransactionsParams(),
    ): Result<ListTransactionsResponsePayload>

    suspend fun makeInvoice(params: MakeInvoiceParams): Result<MakeInvoiceResponsePayload>

    suspend fun lookupInvoice(params: LookupInvoiceParams): Result<LookupInvoiceResponsePayload>

    suspend fun getInfo(): Result<GetInfoResponsePayload>

    suspend fun payInvoice(params: PayInvoiceParams): Result<PayInvoiceResponsePayload>

    suspend fun payKeysend(params: PayKeysendParams): Result<PayKeysendResponsePayload>

    suspend fun multiPayInvoice(params: List<PayInvoiceParams>): Result<List<PayInvoiceResponsePayload>>

    suspend fun multiPayKeysend(params: List<PayKeysendParams>): Result<List<PayKeysendResponsePayload>>
}
