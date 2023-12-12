package net.primal.android.wallet.store.inapp

interface InAppPurchaseBuyContract {
    data class UiState(
        val refreshing: Boolean = false,
    )
}
