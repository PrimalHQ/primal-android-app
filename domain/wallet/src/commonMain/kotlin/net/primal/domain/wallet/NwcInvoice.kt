package net.primal.domain.wallet

data class NwcInvoice(
    val invoice: String,
    val paymentHash: String?,
    val walletId: String,
    val connectionId: String,
    val description: String?,
    val descriptionHash: String?,
    val amountMsats: Long,
    val createdAt: Long,
    val expiresAt: Long,
    val settledAt: Long?,
    val preimage: String?,
    val state: NwcInvoiceState,
)

enum class NwcInvoiceState {
    PENDING,
    SETTLED,
    EXPIRED,
}
