package net.primal.android.wallet.transactions.send.prepare

import net.primal.android.wallet.transactions.send.create.DraftTransaction
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab

interface SendPaymentContract {
    data class UiState(
        val initialTab: SendPaymentTab,
        val currentTab: SendPaymentTab = initialTab,
        val parsing: Boolean = false,
        val error: SendPaymentError? = null,
    ) {
        sealed class SendPaymentError {
            data class NostrUserWithoutLightningAddress(val userDisplayName: String) : SendPaymentError()
            data class ParseException(val cause: Exception) : SendPaymentError()
        }
    }
    sealed class UiEvent {
        data class ProcessProfileData(val profileId: String) : UiEvent()
        data class ProcessTextData(val text: String) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data class DraftTransactionReady(
            val draft: DraftTransaction,
        ) : SideEffect()
    }
}
