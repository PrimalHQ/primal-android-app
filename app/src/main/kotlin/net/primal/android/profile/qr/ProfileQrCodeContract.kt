package net.primal.android.profile.qr

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.domain.wallet.DraftTx

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
        data class NostrLiveStreamDetected(val naddr: String) : SideEffect()
        data class NostrArticleDetected(val naddr: String) : SideEffect()
        data class WalletTxDetected(val draftTx: DraftTx) : SideEffect()
        data class PromoCodeDetected(val promoCode: String) : SideEffect()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onProfileScan: (profileId: String) -> Unit,
        val onNoteScan: (noteId: String) -> Unit,
        val onLiveStreamScan: (naddr: String) -> Unit,
        val onArticleScan: (naddr: String) -> Unit,
        val onDraftTxScan: (draftTx: DraftTx) -> Unit,
        val onPromoCodeScan: (promoCode: String) -> Unit,
    )
}
