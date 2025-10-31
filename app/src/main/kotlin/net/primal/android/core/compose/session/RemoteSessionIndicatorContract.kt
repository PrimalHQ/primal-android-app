package net.primal.android.core.compose.session

interface RemoteSessionIndicatorContract {
    data class UiState(
        val isRemoteSessionActive: Boolean = false,
    )
}
