package net.primal.domain.wallet

sealed class TxRequest(
    open val amountSats: String,
    open val noteRecipient: String?,
    open val noteSelf: String?,
    open val idempotencyKey: String?,
) {
    sealed class Lightning(
        override val amountSats: String,
        override val noteRecipient: String?,
        override val noteSelf: String?,
        override val idempotencyKey: String?,
    ) : TxRequest(amountSats, noteRecipient, noteSelf, idempotencyKey) {
        data class LnInvoice(
            override val amountSats: String,
            override val noteRecipient: String?,
            override val noteSelf: String?,
            override val idempotencyKey: String?,
            val lnInvoice: String,
        ) : Lightning(amountSats, noteRecipient, noteSelf, idempotencyKey)

        data class LnUrl(
            override val amountSats: String,
            override val noteRecipient: String?,
            override val noteSelf: String?,
            override val idempotencyKey: String?,
            val lnUrl: String,
            val lud16: String?,
        ) : Lightning(amountSats, noteRecipient, noteSelf, idempotencyKey)
    }

    data class BitcoinOnChain(
        override val amountSats: String,
        override val noteRecipient: String?,
        override val noteSelf: String?,
        override val idempotencyKey: String?,
        val onChainAddress: String,
        val onChainTierId: String?,
    ) : TxRequest(amountSats, noteRecipient, noteSelf, idempotencyKey)
}
