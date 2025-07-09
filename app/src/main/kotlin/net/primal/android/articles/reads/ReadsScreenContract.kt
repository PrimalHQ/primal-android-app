package net.primal.android.articles.reads

import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.domain.links.CdnImage

interface ReadsScreenContract {
    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val badges: Badges = Badges(),
        val loading: Boolean = false,
    )

    sealed class UiEvent {
        data object RestoreDefaultFeeds : UiEvent()
        data object RefreshReadsFeeds : UiEvent()
    }

    data class ScreenCallbacks(
        val onDrawerQrCodeClick: () -> Unit,
        val onSearchClick: () -> Unit,
        val onArticleClick: (String) -> Unit,
        val onGetPremiumClick: () -> Unit,
    )
}
