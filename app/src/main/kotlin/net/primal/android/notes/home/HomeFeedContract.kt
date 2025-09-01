package net.primal.android.notes.home

import net.primal.android.core.errors.UiError
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.notes.feed.model.StreamPillUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.domain.links.CdnImage

interface HomeFeedContract {

    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val streams: List<StreamPillUi> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val badges: Badges = Badges(),
        val loading: Boolean = true,
        val uiError: UiError? = null,
    )

    sealed class UiEvent {
        data object RequestUserDataUpdate : UiEvent()
        data object RefreshNoteFeeds : UiEvent()
        data object RestoreDefaultNoteFeeds : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data class StartStream(val naddr: String) : SideEffect()
    }

    data class ScreenCallbacks(
        val onDrawerQrCodeClick: () -> Unit,
        val onGoToWallet: () -> Unit,
        val onSearchClick: () -> Unit,
        val onNewPostClick: () -> Unit,
    )
}
