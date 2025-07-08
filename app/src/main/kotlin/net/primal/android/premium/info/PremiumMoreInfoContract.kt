package net.primal.android.premium.info

interface PremiumMoreInfoContract {
    data class ScreenCallbacks(
        val onClose: () -> Unit,
    )
}
