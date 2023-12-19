package net.primal.android.wallet.store.inapp

import android.app.Activity
import net.primal.android.wallet.store.domain.SatsPurchaseQuote
import net.primal.android.wallet.store.play.InAppProduct

interface InAppPurchaseBuyContract {
    data class UiState(
        val minSatsInAppProduct: InAppProduct?,
        val quote: SatsPurchaseQuote? = null,
        val purchasingQuote: SatsPurchaseQuote? = null,
        val error: Throwable? = null,
    )
    sealed class UiEvent {
        data object ClearQuote : UiEvent()
        data object RefreshQuote : UiEvent()
        data class RequestPurchase(val activity: Activity) : UiEvent()
    }
    sealed class SideEffect {
        data object PurchaseConfirmed : SideEffect()
    }
}
