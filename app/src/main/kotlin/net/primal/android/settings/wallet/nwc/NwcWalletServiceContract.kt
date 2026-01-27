package net.primal.android.settings.wallet.nwc

interface NwcWalletServiceContract {

    data class UiState(
        val isCreating: Boolean = false,
        val nwcConnectionUri: String? = null,
        val error: String? = null,
        val walletId: String? = null,
    )

    sealed class UiEvent {
        data object CreateConnection : UiEvent()
        data object DismissError : UiEvent()
    }
}
