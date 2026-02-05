package net.primal.android.wallet.upgrade

import net.primal.domain.wallet.migration.MigrationStep

interface UpgradeWalletContract {

    data class UiState(
        val status: UpgradeWalletStatus = UpgradeWalletStatus.Ready,
        val currentStep: MigrationStep? = null,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data object StartUpgrade : UiEvent()
        data object RetryUpgrade : UiEvent()
    }
}

enum class UpgradeWalletStatus {
    Ready,
    Upgrading,
    Success,
    Failed,
}
