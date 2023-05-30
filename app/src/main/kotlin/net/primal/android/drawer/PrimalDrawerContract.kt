package net.primal.android.drawer

interface PrimalDrawerContract {

    data class UiState(
        val loading: Boolean = false,
    )
}