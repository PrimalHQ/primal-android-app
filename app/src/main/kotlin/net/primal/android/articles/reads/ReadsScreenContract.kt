package net.primal.android.articles.reads

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.user.domain.Badges

interface ReadsScreenContract {
    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val badges: Badges = Badges(),
    )
}
