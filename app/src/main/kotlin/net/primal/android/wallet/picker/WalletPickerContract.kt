package net.primal.android.wallet.picker

import net.primal.domain.wallet.UserWallet

interface WalletPickerContract {
    data class UiState(
        val wallets: List<UserWallet> = emptyList(),
        val activeWalletId: String? = null,
        val registeredWalletId: String? = null,
        val registeredLightningAddress: String? = null,
        val isEditMode: Boolean = false,
        val previewRegisteredWalletId: String? = null,
        val isReassigning: Boolean = false,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data class ChangeActiveWallet(val userWallet: UserWallet) : UiEvent()
        data object EnterEditMode : UiEvent()
        data object CancelEditMode : UiEvent()
        data class SelectWalletForReassignment(val userWallet: UserWallet) : UiEvent()
        data object ConfirmReassignment : UiEvent()
        data object DismissError : UiEvent()
    }
}
