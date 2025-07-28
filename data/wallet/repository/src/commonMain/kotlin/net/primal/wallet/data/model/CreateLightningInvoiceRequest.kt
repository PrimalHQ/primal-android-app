package net.primal.wallet.data.model

import net.primal.domain.wallet.SubWallet

sealed class CreateLightningInvoiceRequest(
    open val description: String? = null,
) {
    data class Primal(
        override val description: String?,
        val subWallet: SubWallet,
        val amountInBtc: String?,
    ) : CreateLightningInvoiceRequest(description)

    data class NWC(
        override val description: String?,
        val amountInMSats: Long,
        val descriptionHash: String?,
        val expiry: Long? = null,
    ) : CreateLightningInvoiceRequest(description)
}
