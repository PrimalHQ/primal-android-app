package net.primal.domain.connections.nostr.model

data class NwcDailyBudgetStatus(
    val connectionId: String,
    val budgetDate: String,
    val dailyLimitSats: Long?,
    val confirmedSpendSats: Long,
    val pendingHoldsSats: Long,
    val availableSats: Long?,
)
