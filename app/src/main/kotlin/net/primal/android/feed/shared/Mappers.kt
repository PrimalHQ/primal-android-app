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
    authorId = this.author.ownerId,
    authorDisplayName = this.author.displayNameUiFriendly(),
    authorInternetIdentifier = this.author.internetIdentifier,
    authorAvatarUrl = this.author.picture,
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
        userReplied = this.userStats?.userReplied ?: false,
        zapsCount = this.postStats?.zaps ?: 0,
        satsZapped = this.postStats?.satsZapped ?: 0,
        userZapped = this.userStats?.userZapped ?: false,
        likesCount = this.postStats?.likes ?: 0,
        userLiked = this.userStats?.userLiked ?: false,
        repostsCount = this.postStats?.reposts ?: 0,
        userReposted = this.userStats?.userReposted ?: false,
    )
)
