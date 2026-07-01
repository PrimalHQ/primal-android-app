package net.primal.android.feeds.dvm

import net.primal.android.core.errors.UiError
import net.primal.android.feeds.dvm.ui.DvmFeedUi

interface DvmFeedListItemContract {

    data class UiState(
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class OnLikeClick(val dvmFeed: DvmFeedUi) : UiEvent()
        data class OnZapClick(
            val dvmFeed: DvmFeedUi,
            val zapDescription: String? = null,
            val zapAmount: ULong? = null,
        ) : UiEvent()
        data object DismissError : UiEvent()
    }
}
