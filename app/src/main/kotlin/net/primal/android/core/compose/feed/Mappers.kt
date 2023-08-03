package net.primal.android.core.compose.feed

import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.NostrResourceUi
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.feed.db.FeedPost
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.profile.db.userNameUiFriendly
import java.time.Instant

fun FeedPost.asFeedPostUi() = FeedPostUi(
    postId = this.data.postId,
    repostId = this.data.repostId,
    repostAuthorId = this.data.repostAuthorId,
    repostAuthorName = this.repostAuthor?.authorNameUiFriendly() ?: this.data.repostAuthorId?.asEllipsizedNpub(),
    authorId = this.author?.ownerId ?: this.data.authorId,
    authorName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
    authorHandle = this.author?.userNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
    authorInternetIdentifier = this.author?.internetIdentifier,
    authorAvatarUrl = this.author?.picture,
    timestamp = Instant.ofEpochSecond(this.data.createdAt),
    content = this.data.content,
    authorMediaResources = this.authorResources.map {
        MediaResourceUi(
            url = it.url,
            mimeType = it.contentType,
            variants = it.variants ?: emptyList(),
        )
    },
    mediaResources = this.postResources.map {
        MediaResourceUi(
            url = it.url,
            mimeType = it.contentType,
            variants = it.variants ?: emptyList(),
        )
    },
    nostrResources = this.nostrUris.map {
        NostrResourceUi(
            uri = it.uri,
            referencedPost = it.referencedPost,
            referencedUser = it.referencedUser,
        )
    },
    stats = FeedPostStatsUi(
        repliesCount = this.postStats?.replies ?: 0,
        userReplied = this.userStats?.userReplied ?: false,
        zapsCount = this.postStats?.zaps ?: 0,
        satsZapped = this.postStats?.satsZapped ?: 0,
        userZapped = this.userStats?.userZapped ?: false,
        likesCount = this.postStats?.likes ?: 0,
        userLiked = this.userStats?.userLiked ?: false,
        repostsCount = this.postStats?.reposts ?: 0,
        userReposted = this.userStats?.userReposted ?: false,
    ),
    hashtags = this.data.hashtags,
    rawNostrEventJson = this.data.raw,
)
