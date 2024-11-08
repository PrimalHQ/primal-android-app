package net.primal.android.premium.manage

interface PremiumManageContract {
    enum class ManageDestination {
        MediaManagement,
        PremiumRelay,
        ContactListBackup,
        ContentBackup,
        ManageSubscription,
        ChangePrimalName,
        ExtendSubscription,
        LegendaryProfileCustomization,
    }
}
