package net.primal.android.wallet.receive

interface ReceivePaymentContract {
    data class UiState(
        val loading: Boolean = true,
        val lightningAddress: String? = null,
    )
}
