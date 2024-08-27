package net.primal.android.articles.reads

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.user.domain.Badges

interface ReadsScreenContract {

    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val activeFeed: FeedUi? = null,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data class ChangeFeed(val feed: FeedUi) : UiEvent()
    }
}
