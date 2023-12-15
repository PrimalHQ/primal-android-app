package net.primal.android.scan

import net.primal.android.scan.analysis.QrCodeResult

interface ScanContract {

    data class UiState(
        val scanning: Boolean = true,
    )

    sealed class UiEvent {
        data class ProcessScannedData(val result: QrCodeResult) : UiEvent()
    }

    sealed class SideEffect {
        data object ScanningCompleted : SideEffect()
    }
}
