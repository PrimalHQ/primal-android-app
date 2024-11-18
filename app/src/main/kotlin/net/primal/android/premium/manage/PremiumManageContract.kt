package net.primal.android.premium.manage

interface PremiumManageContract {

    data class UiState(
        val primalName: String = "",
        val isLegend: Boolean = false,
        val isRecurring: Boolean = false,
    )

    sealed class ManageDestination {
        data object MediaManagement : ManageDestination()
        data object PremiumRelay : ManageDestination()
        data object ContactListBackup : ManageDestination()
        data object ContentBackup : ManageDestination()
        data object ManageSubscription : ManageDestination()
        data object ChangePrimalName : ManageDestination()
        data class ExtendSubscription(val primalName: String) : ManageDestination()
        data object LegendaryProfileCustomization : ManageDestination()
        data object BecomeALegend : ManageDestination()
    }
}
