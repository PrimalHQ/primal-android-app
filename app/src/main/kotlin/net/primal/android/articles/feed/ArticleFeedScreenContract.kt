package net.primal.android.articles.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.user.domain.Badges

interface ArticleFeedScreenContract {

    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val zappingState: ZappingState = ZappingState(),
        val badges: Badges = Badges(),
        val articles: Flow<PagingData<FeedArticleUi>>,
    )

    sealed class UiEvent
}
