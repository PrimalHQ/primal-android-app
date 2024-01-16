package net.primal.android.wallet.transactions.details

interface TransactionDetailsContract {
    data class UiState(
        val loading: Boolean = false,
    )
    sealed class UiEvent
}
