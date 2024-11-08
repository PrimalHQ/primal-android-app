package net.primal.android.premium.support

interface SupportPrimalContract {

    data class UiState(
        val hasMembership: Boolean = false,
        val isPrimalLegend: Boolean = false,
    )

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onBuySubscription: () -> Unit,
        val onBecomeLegend: () -> Unit,
    )
}
