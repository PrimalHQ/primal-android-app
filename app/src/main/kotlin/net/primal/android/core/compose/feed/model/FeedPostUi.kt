package net.primal.android.core.compose.feed.model

import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.compose.media.model.asMediaResourceUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.feed.db.FeedPost
import java.time.Instant

data class FeedPostUi(
    val postId: String,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val repostAuthorName: String? = null,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorInternetIdentifier: String? = null,
    val authorLightningAddress: String? = null,
    val authorAvatarUrl: String? = null,
    val authorMediaResources: List<MediaResourceUi> = emptyList(),
    val mediaResources: List<MediaResourceUi> = emptyList(),
    val nostrResources: List<NostrResourceUi> = emptyList(),
    val timestamp: Instant,
    val content: String,
    val stats: FeedPostStatsUi,
    val hashtags: List<String> = emptyList(),
    val rawNostrEventJson: String,
)

fun FeedPost.asFeedPostUi() = FeedPostUi(
    postId = this.data.postId,
    repostId = this.data.repostId,
    repostAuthorId = this.data.repostAuthorId,
    repostAuthorName = this.repostAuthor?.authorNameUiFriendly() ?: this.data.repostAuthorId?.asEllipsizedNpub(),
    authorId = this.author?.ownerId ?: this.data.authorId,
    authorName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
    authorHandle = this.author?.usernameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
    authorInternetIdentifier = this.author?.internetIdentifier,
    authorLightningAddress = this.author?.lightningAddress,
    authorAvatarUrl = this.author?.picture,
    timestamp = Instant.ofEpochSecond(this.data.createdAt),
    content = this.data.content,
    authorMediaResources = this.authorResources.map { it.asMediaResourceUi() },
    mediaResources = this.postResources.map { it.asMediaResourceUi() },
    nostrResources = this.nostrUris.map { it.asNostrResourceUi() },
    stats = FeedPostStatsUi.from(postStats = this.postStats, userStats = this.userStats),
    hashtags = this.data.hashtags,
    rawNostrEventJson = this.data.raw,
)
