package net.primal.android.settings.developer

import java.io.File

interface DeveloperToolsContract {
    data class UiState(
        val isLoggingEnabled: Boolean = false,
        val logFileCount: Int = 0,
        val totalLogSizeBytes: Long = 0L,
        val isExporting: Boolean = false,
    )

    sealed class UiEvent {
        data class ToggleLogging(val enabled: Boolean) : UiEvent()
        data object ExportLogs : UiEvent()
        data object ClearLogs : UiEvent()
    }

    sealed class SideEffect {
        data class ShareLogs(val file: File) : SideEffect()
        data object NoLogsToExport : SideEffect()
        data object ExportFailed : SideEffect()
    }
}
