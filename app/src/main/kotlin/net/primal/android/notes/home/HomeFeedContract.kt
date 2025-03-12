package net.primal.android.notes.home

import net.primal.android.events.domain.CdnImage
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges

interface HomeFeedContract {

    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val badges: Badges = Badges(),
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data object RequestUserDataUpdate : UiEvent()
        data object RefreshNoteFeeds : UiEvent()
        data object RestoreDefaultNoteFeeds : UiEvent()
    }
}
