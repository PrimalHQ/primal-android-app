package net.primal.android.wallet.transactions.send.prepare

import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab

interface SendPaymentContract {
    data class UiState(
        val initialTab: SendPaymentTab,
        val currentTab: SendPaymentTab = initialTab,
        val parsing: Boolean = false,
        val error: SendPaymentError? = null,
    ) {
        sealed class SendPaymentError {
            data class LightningAddressNotFound(val userDisplayName: String?) : SendPaymentError()
            data class ParseException(val cause: Exception) : SendPaymentError()
        }
    }
    sealed class UiEvent {
        data class QrCodeDetected(val result: QrCodeResult) : UiEvent()
        data class ProcessProfileData(val profileId: String) : UiEvent()
        data class ProcessTextData(val text: String) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data class PromoCodeDetected(val promoCode: String) : SideEffect()
        data class DraftTransactionReady(val draft: DraftTx) : SideEffect()
    }
}
