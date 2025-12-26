package net.primal.android.core.compose.session

interface RemoteSessionIndicatorContract {
    data class UiState(
        val isRemoteSessionActive: Boolean = false,
        val activeAppName: String? = null,
        val activeAppIconUrl: String? = null,
    )
}
