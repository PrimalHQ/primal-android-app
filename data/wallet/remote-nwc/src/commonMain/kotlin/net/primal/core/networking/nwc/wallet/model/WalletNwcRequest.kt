package net.primal.core.networking.nwc.wallet.model

import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceParams
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.NwcEncryptionScheme
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.networking.nwc.nip47.PayKeysendParams
import net.primal.domain.connections.nostr.model.NwcConnection

sealed class WalletNwcRequest {
    abstract val eventId: String
    abstract val connection: NwcConnection
    abstract val encryptionScheme: NwcEncryptionScheme

    data class PayInvoice(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: PayInvoiceParams,
    ) : WalletNwcRequest()

    data class PayKeysend(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: PayKeysendParams,
    ) : WalletNwcRequest()

    data class MakeInvoice(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: MakeInvoiceParams,
    ) : WalletNwcRequest()

    data class LookupInvoice(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: LookupInvoiceParams,
    ) : WalletNwcRequest()

    data class ListTransactions(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: ListTransactionsParams,
    ) : WalletNwcRequest()

    data class GetBalance(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
    ) : WalletNwcRequest()

    data class GetInfo(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
    ) : WalletNwcRequest()

    data class MultiPayInvoice(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: List<PayInvoiceParams>,
    ) : WalletNwcRequest()

    data class MultiPayKeysend(
        override val eventId: String,
        override val connection: NwcConnection,
        override val encryptionScheme: NwcEncryptionScheme,
        val params: List<PayKeysendParams>,
    ) : WalletNwcRequest()
}
