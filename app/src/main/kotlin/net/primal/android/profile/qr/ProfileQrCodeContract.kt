package net.primal.android.profile.qr

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.wallet.domain.DraftTx

interface ProfileQrCodeContract {
    data class UiState(
        val profileId: String,
        val profileDetails: ProfileDetailsUi? = null,
    )

    sealed class UiEvent {
        data class ProcessQrCodeResult(val result: QrCodeResult) : UiEvent()
    }

    sealed class SideEffect {
        data class NostrProfileDetected(val profileId: String) : SideEffect()
        data class NostrNoteDetected(val noteId: String) : SideEffect()
        data class WalletTxDetected(val draftTx: DraftTx) : SideEffect()
    }
}
