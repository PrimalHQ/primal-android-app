package net.primal.android.premium.manage.order

import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.domain.PremiumPurchaseOrder

interface PremiumOrderHistoryContract {

    data class UiState(
        val fetchingHistory: Boolean = true,
        val orders: List<PremiumPurchaseOrder> = emptyList(),
        val isRecurringSubscription: Boolean = false,
        val cancellingSubscription: Boolean = false,
        val error: MembershipError? = null,
    )

    sealed class UiEvent {
        data object CancelSubscription : UiEvent()
    }
}
