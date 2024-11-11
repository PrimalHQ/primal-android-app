package net.primal.android.premium.support

interface SupportPrimalContract {

    data class UiState(
        val primalName: String? = null,
        val hasMembership: Boolean = false,
        val isPrimalLegend: Boolean = false,
    )

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onExtendSubscription: (primalName: String) -> Unit,
        val onBecomeLegend: () -> Unit,
    )
}
