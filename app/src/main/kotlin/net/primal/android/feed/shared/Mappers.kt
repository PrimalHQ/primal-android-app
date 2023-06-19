package net.primal.android.feed.shared

import net.primal.android.feed.db.FeedPost
import net.primal.android.feed.shared.model.FeedPostResource
import net.primal.android.feed.shared.model.FeedPostStatsUi
import net.primal.android.feed.shared.model.FeedPostUi
import net.primal.android.nostr.ext.asEllipsizedNpub
import net.primal.android.nostr.ext.displayNameUiFriendly
import java.time.Instant


fun FeedPost.asFeedPostUi() = FeedPostUi(
    postId = this.data.postId,
    repostId = this.data.repostId,
    repostAuthorDisplayName = this.repostAuthor?.displayNameUiFriendly()
        ?: this.data.repostAuthorId?.asEllipsizedNpub(),
    authorDisplayName = this.author?.displayNameUiFriendly()
        ?: this.data.authorId.asEllipsizedNpub(),
    authorInternetIdentifier = this.author?.internetIdentifier,
    authorAvatarUrl = this.author?.picture,
    timestamp = Instant.ofEpochSecond(this.data.createdAt),
    content = this.data.content,
    resources = this.resources.map {
        FeedPostResource(
            url = it.url,
            mimeType = it.contentType,
            variants = it.variants ?: emptyList(),
        )
    },
    stats = FeedPostStatsUi(
        repliesCount = this.postStats?.replies ?: 0,
        userReplied = false,
        zapsCount = this.postStats?.zaps ?: 0,
        satsZapped = this.postStats?.satsZapped ?: 0,
        userZapped = false,
        likesCount = this.postStats?.likes ?: 0,
        userLiked = false,
        repostsCount = this.postStats?.reposts ?: 0,
        userReposted = false,
    )
)