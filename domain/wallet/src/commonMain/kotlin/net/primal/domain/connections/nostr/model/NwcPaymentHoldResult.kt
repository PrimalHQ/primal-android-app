package net.primal.domain.connections.nostr.model

sealed class NwcPaymentHoldResult {
    data class Placed(
        val holdId: String,
        val amountSats: Long,
        val remainingBudget: Long,
    ) : NwcPaymentHoldResult()

    data class InsufficientBudget(
        val requested: Long,
        val available: Long,
    ) : NwcPaymentHoldResult()
}
