package net.primal.android.settings.wallet

interface WalletContract {
    data class UiState(
        val nwcUrl: String? = null,
        val isWalletConnected: Boolean = false
    )
}