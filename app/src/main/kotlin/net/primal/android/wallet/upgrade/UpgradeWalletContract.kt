package net.primal.android.wallet.upgrade

interface UpgradeWalletContract {

    data class UiState(
        val status: UpgradeWalletStatus = UpgradeWalletStatus.Upgrading,
        val error: Throwable? = null,
    )
}

enum class UpgradeWalletStatus {
    Upgrading,
    Success,
    Failed,
}
