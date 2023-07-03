package net.primal.android.core.compose.feed.model

import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String?,
    val repostAuthorDisplayName: String? = null,
    val authorId: String,
    val authorDisplayName: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val resources: List<FeedPostResource>,
    val timestamp: Instant,
    val stats: FeedPostStatsUi,
)





