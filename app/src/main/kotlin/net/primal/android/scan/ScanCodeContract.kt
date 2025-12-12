package net.primal.android.scan

import net.primal.android.core.errors.UiError
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.domain.wallet.DraftTx

interface ScanCodeContract {
    data class UiState(
        val scanMode: ScanMode = ScanMode.Anything,
        val userState: UserState = UserState.NoUser,
        val requiresPrimalWallet: Boolean = false,
        val scannedValue: String? = null,
        val welcomeMessage: String? = null,
        val loading: Boolean = false,
        val showErrorBadge: Boolean = false,
        val error: UiError? = null,
        val stageStack: List<ScanCodeStage> = listOf(
            ScanCodeStage.ScanCamera,
        ),
        val promoCodeBenefits: List<PromoCodeBenefit> = emptyList(),
    ) {
        fun getStage() = stageStack.last()
    }

    sealed class UiEvent {
        data object DismissError : UiEvent()
        data object PreviousStage : UiEvent()
        data object GoToManualInput : UiEvent()
        data class QrCodeDetected(val result: QrCodeResult) : UiEvent()
        data class ProcessCode(val value: String) : UiEvent()
        data class ApplyPromoCode(val code: String) : UiEvent()
    }

    sealed class SideEffect {
        data object PromoCodeApplied : SideEffect()
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
        Success,
    }

    enum class ScanMode {
        Anything,
        RemoteLogin,
    }

    enum class UserState {
        NoUser,
        UserWithPrimalWallet,
        UserWithoutPrimalWallet,
    }

    sealed class PromoCodeBenefit {
        data class PrimalPremium(val durationInMonths: Int) : PromoCodeBenefit()
        data class WalletBalance(val sats: Double) : PromoCodeBenefit()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val navigateToOnboarding: (String?) -> Unit,
        val navigateToWalletOnboarding: (String?) -> Unit,
        val onNostrConnectRequest: (url: String) -> Unit,
        val onDraftTransactionReady: (draft: DraftTx) -> Unit,
        val onProfileScan: (profileId: String) -> Unit,
        val onNoteScan: (noteId: String) -> Unit,
        val onArticleScan: (naddr: String) -> Unit,
        val onLiveStreamScan: (naddr: String) -> Unit,
    )
}
