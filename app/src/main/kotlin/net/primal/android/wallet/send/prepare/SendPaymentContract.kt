package net.primal.android.wallet.send.prepare

import net.primal.android.wallet.send.create.DraftTransaction
import net.primal.android.wallet.send.prepare.tabs.SendPaymentTab

interface SendPaymentContract {
    data class UiState(
        val initialTab: SendPaymentTab,
        val currentTab: SendPaymentTab = initialTab,
        val parsing: Boolean = false,
    )
    sealed class UiEvent {
        data class ProcessProfileData(val profileId: String) : UiEvent()
        data class ProcessTextData(val text: String) : UiEvent()
    }

    sealed class SideEffect {
        data class DraftTransactionReady(
            val draft: DraftTransaction,
        ) : SideEffect()
    }
}
