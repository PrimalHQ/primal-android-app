package net.primal.android.articles.feed

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.user.domain.Badges

interface ArticleFeedScreenContract {

    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val zappingState: ZappingState = ZappingState(),
        val badges: Badges = Badges(),
    )

    sealed class UiEvent
}
