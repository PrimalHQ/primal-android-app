package net.primal.android.wallet.backup

interface WalletBackupContract {
    data class UiState(
        val currentStep: BackupStep = BackupStep.Welcome,
        val seedPhrase: List<String> = emptyList(),
        val verificationIndices: List<Int> = emptyList(),
    )

    sealed class UiEvent {
        data object ProceedToPhraseDisplay : UiEvent()
        data object ProceedToVerification : UiEvent()
        data object ProceedToConfirmation : UiEvent()
        data object RequestPreviousStep : UiEvent()
        data object CompleteBackup : UiEvent()
    }

    sealed class SideEffect {
        data object BackupCompleted : SideEffect()
    }

    enum class BackupStep {
        Welcome,
        SeedPhrase,
        Verify,
        Confirm,
    }

    data class ScreenCallbacks(
        val onBackupComplete: () -> Unit,
        val onClose: () -> Unit,
    )
}
