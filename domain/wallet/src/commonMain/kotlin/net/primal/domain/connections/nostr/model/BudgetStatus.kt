package net.primal.domain.connections.nostr.model

data class BudgetStatus(
    val connectionId: String,
    val budgetDate: String,
    val dailyLimitSats: Long?,
    val confirmedSpendSats: Long,
    val pendingReservationsSats: Long,
    val availableSats: Long?,
)
