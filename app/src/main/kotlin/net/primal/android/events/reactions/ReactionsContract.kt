package net.primal.android.events.reactions

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.events.ui.EventZapUiModel
import net.primal.domain.nostr.ReactionType

interface ReactionsContract {
    data class UiState(
        val zaps: Flow<PagingData<EventZapUiModel>>,
        val loading: Boolean = true,
        val likes: List<EventActionUi> = emptyList(),
        val reposts: List<EventActionUi> = emptyList(),
        val initialReactionType: ReactionType = ReactionType.ZAPS,
    )

    data class ScreenCallbacks(
        val onProfileClick: (profileId: String) -> Unit,
        val onClose: () -> Unit,
    )
}
