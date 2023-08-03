package net.primal.android.core.compose.feed.model

import net.primal.android.core.compose.media.model.MediaResourceUi
import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String?,
    val repostAuthorId: String? = null,
    val repostAuthorDisplayName: String? = null,
    val authorId: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val authorResources: List<MediaResourceUi>,
    val postResources: List<MediaResourceUi>,
    val nostrUris: List<NostrUriUi>,
    val timestamp: Instant,
    val stats: FeedPostStatsUi,
    val hashtags: List<String>,
    val rawNostrEventJson: String,
)
