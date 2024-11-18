package net.primal.android.premium.manage

interface PremiumManageContract {

    data class UiState(
        val isLegend: Boolean = false,
        val isRecurring: Boolean = false,
    )

    enum class ManageDestination {
        MediaManagement,
        PremiumRelay,
        ContactListBackup,
        ContentBackup,
        ManageSubscription,
        ChangePrimalName,
        ExtendSubscription,
        LegendaryProfileCustomization,
        BecomeALegend,
    }
}
