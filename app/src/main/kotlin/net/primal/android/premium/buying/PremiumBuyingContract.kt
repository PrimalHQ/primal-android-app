package net.primal.android.premium.buying

import net.primal.android.wallet.store.domain.SubscriptionProduct

interface PremiumBuyingContract {
    data class UiState(
        val subscriptions: List<SubscriptionProduct> = emptyList(),
        val stage: PremiumStage = PremiumStage.Home,
        val primalName: String? = null,
    )

    sealed class UiEvent {
        data class MoveToPremiumStage(val stage: PremiumStage) : UiEvent()
        data class SetPrimalName(val primalName: String) : UiEvent()
    }

    enum class PremiumStage {
        Home,
        FindPrimalName,
        Purchase,
        Success,
    }
}
