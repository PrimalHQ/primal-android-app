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

interface NwcApi {

    suspend fun getBalance(): NwcResult<GetBalanceResponsePayload>

    suspend fun listTransactions(
        params: ListTransactionsParams = ListTransactionsParams(),
    ): NwcResult<ListTransactionsResponsePayload>

    suspend fun makeInvoice(params: MakeInvoiceParams): NwcResult<MakeInvoiceResponsePayload>

    suspend fun lookupInvoice(params: LookupInvoiceParams): NwcResult<LookupInvoiceResponsePayload>

    suspend fun getInfo(): NwcResult<GetInfoResponsePayload>

    suspend fun payInvoice(params: PayInvoiceParams): NwcResult<PayInvoiceResponsePayload>

    suspend fun payKeysend(params: PayKeysendParams): NwcResult<PayKeysendResponsePayload>

    suspend fun multiPayInvoice(params: List<PayInvoiceParams>): NwcResult<List<PayInvoiceResponsePayload>>

    suspend fun multiPayKeysend(params: List<PayKeysendParams>): NwcResult<List<PayKeysendResponsePayload>>
}
