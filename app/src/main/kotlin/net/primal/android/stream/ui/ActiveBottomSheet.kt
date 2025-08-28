package net.primal.android.stream.ui

import net.primal.android.events.ui.EventZapUiModel

sealed interface ActiveBottomSheet {
    data object None : ActiveBottomSheet
    data object StreamInfo : ActiveBottomSheet
    data object StreamSettings : ActiveBottomSheet
    data class ChatDetails(val message: ChatMessageUi) : ActiveBottomSheet
    data class ZapDetails(val zap: EventZapUiModel) : ActiveBottomSheet
}
