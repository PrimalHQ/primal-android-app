package net.primal.android.articles.reads

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.user.domain.Badges

interface ReadsScreenContract {
    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val badges: Badges = Badges(),
        val loading: Boolean = false,
    )

    sealed class UiEvent {
        data object RestoreDefaultFeeds : UiEvent()
        data object RefreshReadsFeeds : UiEvent()
    }
}
