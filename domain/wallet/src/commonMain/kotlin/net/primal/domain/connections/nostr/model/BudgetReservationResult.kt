package net.primal.domain.connections.nostr.model

sealed class BudgetReservationResult {
    data object Unlimited : BudgetReservationResult()

    data class Reserved(
        val reservationId: String,
        val amountSats: Long,
        val remainingBudget: Long,
    ) : BudgetReservationResult()

    data class InsufficientBudget(
        val requested: Long,
        val available: Long,
    ) : BudgetReservationResult()
}
