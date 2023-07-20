package net.primal.android.core.compose.feed.model

import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.profile.details.model.ProfileDetailsUi
import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String?,
    val repostAuthorId: String? = null,
    val repostAuthorDisplayName: String? = null,
    val authorId: String,
    val authorDisplayName: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val authorResources: List<MediaResourceUi>,
    val postResources: List<MediaResourceUi>,
    val profileLinks: List<ProfileLinkUi>,
    val timestamp: Instant,
    val stats: FeedPostStatsUi,
)
