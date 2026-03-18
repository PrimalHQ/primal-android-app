package net.primal.android.wallet.picker

import net.primal.domain.wallet.Wallet

interface WalletPickerContract {
    data class UiState(
        val wallets: List<Wallet> = emptyList(),
        val activeWalletId: String? = null,
        val registeredWalletId: String? = null,
        val registeredLightningAddress: String? = null,
        val isEditMode: Boolean = false,
        val previewRegisteredWalletId: String? = null,
        val isReassigning: Boolean = false,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data class ChangeActiveWallet(val wallet: Wallet) : UiEvent()
        data object EnterEditMode : UiEvent()
        data object CancelEditMode : UiEvent()
        data class SelectWalletForReassignment(val wallet: Wallet) : UiEvent()
        data object ConfirmReassignment : UiEvent()
        data object DismissError : UiEvent()
    }
}
