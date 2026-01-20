package net.primal.android.wallet.restore

interface RestoreWalletContract {
    data class UiState(
        val currentStage: RestoreStage = RestoreStage.MnemonicInput,
        val mnemonic: String = "",
        val mnemonicValidation: MnemonicValidation = MnemonicValidation.Empty,
    ) {
        sealed class MnemonicValidation {
            data object Empty : MnemonicValidation()
            data object Valid : MnemonicValidation()
            data class Invalid(val message: String) : MnemonicValidation()
        }
    }

    sealed class UiEvent {
        data class MnemonicChange(val mnemonic: String) : UiEvent()
        data object RestoreWalletClick : UiEvent()
    }

    sealed class SideEffect {
        data object RestoreSuccess : SideEffect()
    }

    enum class RestoreStage {
        MnemonicInput,
        Restoring,
    }
}
