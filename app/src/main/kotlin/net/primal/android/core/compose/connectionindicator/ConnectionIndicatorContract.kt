package net.primal.android.core.compose.connectionindicator

interface ConnectionIndicatorContract {
    data class UiState(
        val hasConnection: Boolean = true,
    )
}
