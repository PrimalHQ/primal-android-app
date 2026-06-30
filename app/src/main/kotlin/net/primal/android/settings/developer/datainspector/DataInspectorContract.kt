package net.primal.android.settings.developer.datainspector

import java.io.File

interface DataInspectorContract {

    data class DataFile(
        val absolutePath: String,
        val relativePath: String,
        val topLevelFolder: String,
        val sizeBytes: Long,
    )

    data class UiState(
        val isLoading: Boolean = true,
        val files: List<DataFile> = emptyList(),
        val totalSizeBytes: Long = 0L,
    )

    sealed class UiEvent {
        data class ExportFile(val absolutePath: String) : UiEvent()
    }

    sealed class SideEffect {
        data class ShareFile(val file: File) : SideEffect()
        data object ExportFailed : SideEffect()
    }
}
