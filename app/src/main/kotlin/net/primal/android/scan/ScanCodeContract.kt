package net.primal.android.scan

import net.primal.android.core.errors.UiError
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.domain.wallet.DraftTx

interface ScanCodeContract {
    data class UiState(
        val scanMode: ScanMode = ScanMode.Anything,
        val scannedValue: String? = null,
        val loading: Boolean = false,
        val showErrorBadge: Boolean = false,
        val error: UiError? = null,
        val stageStack: List<ScanCodeStage> = listOf(
            ScanCodeStage.ScanCamera,
        ),
    ) {
        fun getStage() = stageStack.last()
    }

    sealed class UiEvent {
        data object DismissError : UiEvent()
        data object PreviousStage : UiEvent()
        data object GoToManualInput : UiEvent()
        data class QrCodeDetected(val result: QrCodeResult) : UiEvent()
        data class ProcessCode(val value: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NostrConnectRequest(val url: String) : SideEffect()
        data class DraftTransactionReady(val draft: DraftTx) : SideEffect()
        data class NostrProfileDetected(val profileId: String) : SideEffect()
        data class NostrNoteDetected(val noteId: String) : SideEffect()
        data class NostrArticleDetected(val naddr: String) : SideEffect()
        data class NostrLiveStreamDetected(val naddr: String) : SideEffect()
    }

    enum class ScanCodeStage {
        ScanCamera,
        ManualInput,
    }

    enum class ScanMode {
        Anything,
        RemoteLogin,
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onNostrConnectRequest: (url: String) -> Unit,
        val onDraftTransactionReady: (draft: DraftTx) -> Unit,
        val onProfileScan: (profileId: String) -> Unit,
        val onNoteScan: (noteId: String) -> Unit,
        val onArticleScan: (naddr: String) -> Unit,
        val onLiveStreamScan: (naddr: String) -> Unit,
    )
}
