package net.primal.android.premium.manage.order

import net.primal.android.premium.domain.MembershipError

interface PremiumOrderHistoryContract {

    data class UiState(
        val isSubscription: Boolean = false,
        val cancellingSubscription: Boolean = false,
        val error: MembershipError? = null,
    )

    sealed class UiEvent {
        data object CancelSubscription : UiEvent()
    }
}
