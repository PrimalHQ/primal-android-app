package net.primal.android.discuss.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.user.domain.Badges

interface FeedContract {
    data class UiState(
        val feedPostsCount: Int = 0,
        val feedTitle: String = "",
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val walletConnected: Boolean = false,
        val defaultZapAmount: ULong? = null,
        val zapOptions: List<ULong> = emptyList(),
        val posts: Flow<PagingData<FeedPostUi>>,
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
        val badges: Badges = Badges(),
        val error: FeedError? = null,
    ) {
        sealed class FeedError {
            data class MissingLightningAddress(val cause: Throwable) : FeedError()
            data class InvalidZapRequest(val cause: Throwable) : FeedError()
            data class FailedToPublishZapEvent(val cause: Throwable) : FeedError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : FeedError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : FeedError()
            data class MissingRelaysConfiguration(val cause: Throwable) : FeedError()
            data class FailedToMuteUser(val cause: Throwable) : FeedError()
        }
    }

    sealed class UiEvent {
        data object FeedScrolledToTop : UiEvent()
        data object RequestUserDataUpdate : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String,
        ) : UiEvent()

        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val zapAmount: ULong?,
            val zapDescription: String?,
        ) : UiEvent()

        data class MuteAction(val userId: String) : UiEvent()
    }
}
