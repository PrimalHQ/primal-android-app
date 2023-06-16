package net.primal.android.feed.ui.model

import net.primal.android.nostr.model.primal.PrimalResourceVariant
import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String?,
    val repostAuthorDisplayName: String? = null,
    val authorDisplayName: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val resources: List<PostResource>,
    val timestamp: Instant,
    val stats: FeedPostStatsUi,
)

data class FeedPostStatsUi(
    val repliesCount: Int = 0,
    val userReplied: Boolean = false,
    val zapsCount: Int = 0,
    val satsZapped: Int = 0,
    val userZapped: Boolean = false,
    val likesCount: Int = 0,
    val userLiked: Boolean = false,
    val repostsCount: Int = 0,
    val userReposted: Boolean = false,
)

data class PostResource(
    val url: String,
    val mimeType: String? = null,
    val variants: List<PrimalResourceVariant> = emptyList(),
)

data class FeedPostsSyncStats(
    val postsCount: Int = 0,
    val postIds: List<String> = emptyList(),
    val avatarUrls: List<String> = emptyList(),
)