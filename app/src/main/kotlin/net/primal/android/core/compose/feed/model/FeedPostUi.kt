package net.primal.android.core.compose.feed.model

import net.primal.android.core.compose.media.model.MediaResourceUi
import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String?,
    val repostAuthorId: String? = null,
    val repostAuthorName: String? = null,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorInternetIdentifier: String? = null,
    val authorLightningAddress: String? = null,
    val authorAvatarUrl: String? = null,
    val authorMediaResources: List<MediaResourceUi>,
    val mediaResources: List<MediaResourceUi>,
    val nostrResources: List<NostrResourceUi>,
    val timestamp: Instant,
    val content: String,
    val stats: FeedPostStatsUi,
    val hashtags: List<String>,
    val rawNostrEventJson: String,
)
