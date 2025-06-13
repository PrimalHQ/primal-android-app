package net.primal.wallet.data.repository.mappers.remote

import net.primal.domain.wallet.LnInvoiceCreateResult as LightningInvoiceResultDO
import net.primal.wallet.data.remote.model.LightningInvoiceResponse

internal fun LightningInvoiceResponse.asLightingInvoiceResultDO(): LightningInvoiceResultDO {
    return LightningInvoiceResultDO(
        invoice = this.lnInvoice,
        description = this.description,
    )
}
