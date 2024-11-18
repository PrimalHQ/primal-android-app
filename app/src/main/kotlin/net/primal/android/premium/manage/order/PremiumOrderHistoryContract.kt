package net.primal.android.premium.manage.order

import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.domain.PremiumPurchaseOrder

interface PremiumOrderHistoryContract {

    data class UiState(
        val fetchingHistory: Boolean = true,
        val primalName: String = "",
        val orders: List<PremiumPurchaseOrder> = emptyList(),
        val subscriptionOrigin: String? = null,
        val isRecurringSubscription: Boolean = false,
        val isLegend: Boolean = false,
        val cancellingSubscription: Boolean = false,
        val expiresAt: Long? = null,
        val error: MembershipError? = null,
    )

    sealed class UiEvent {
        data object CancelSubscription : UiEvent()
    }
}
