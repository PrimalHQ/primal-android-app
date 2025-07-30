package net.primal.domain.wallet

sealed class TxRequest(
    open val amountSats: String = "0",
    open val noteRecipient: String?,
    open val noteSelf: String?,
) {
    sealed class Lightning(
        override val amountSats: String = "0",
        override val noteRecipient: String?,
        override val noteSelf: String?,
    ) : TxRequest(amountSats, noteRecipient, noteSelf) {
        data class LnInvoice(
            override val amountSats: String,
            override val noteRecipient: String?,
            override val noteSelf: String?,
            val lnInvoice: String,
        ) : Lightning(amountSats, noteRecipient, noteSelf)

        data class LnUrl(
            override val amountSats: String,
            override val noteRecipient: String?,
            override val noteSelf: String?,
            val lnUrl: String,
            val lud16: String?,
        ) : Lightning(amountSats, noteRecipient, noteSelf)
    }

    data class BitcoinOnChain(
        override val amountSats: String,
        override val noteRecipient: String?,
        override val noteSelf: String?,
        val onChainAddress: String,
        val onChainTier: String?,
    ) : TxRequest(amountSats, noteRecipient, noteSelf)
}
