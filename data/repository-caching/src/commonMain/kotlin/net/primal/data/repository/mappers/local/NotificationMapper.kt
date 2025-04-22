package net.primal.data.repository.mappers.local

import kotlinx.datetime.Instant
import net.primal.data.local.dao.notifications.Notification as NotificationPO
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.notifications.Notification as NotificationDO
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedPostAuthor
import net.primal.domain.posts.FeedPostStats

fun NotificationPO.asNotificationDO(): NotificationDO {
    return NotificationDO(
        notificationId = this.data.notificationId,
        ownerId = this.data.ownerId,
        createdAt = this.data.createdAt,
        type = this.data.type,
        seenGloballyAt = this.data.seenGloballyAt,
        actionUserId = this.data.actionUserId,
        actionPostId = this.data.actionPostId,
        satsZapped = this.data.satsZapped,
        actionByUser = this.actionByUser?.asProfileDataDO(),
        actionOnPost = this.actionPost?.let { post ->
            FeedPost(
                eventId = post.postId,
                author = FeedPostAuthor(
                    authorId = post.authorId,
                    handle = post.authorId.asEllipsizedNpub(),
                    displayName = post.authorId.asEllipsizedNpub(),
                ),
                content = post.content,
                tags = post.tags,
                timestamp = Instant.fromEpochSeconds(post.createdAt),
                rawNostrEvent = post.raw,
                hashtags = post.hashtags,
                replyToAuthor = post.replyToAuthorId?.let {
                    FeedPostAuthor(
                        authorId = it,
                        handle = it.asEllipsizedNpub(),
                        displayName = it.asEllipsizedNpub(),
                    )
                },
                stats = FeedPostStats(
                    repliesCount = this.actionPostStats?.replies ?: 0,
                    userReplied = this.actionPostUserStats?.replied == true,
                    zapsCount = this.actionPostStats?.zaps ?: 0,
                    satsZapped = this.actionPostStats?.satsZapped ?: 0,
                    userZapped = this.actionPostUserStats?.zapped == true,
                    likesCount = this.actionPostStats?.likes ?: 0,
                    userLiked = this.actionPostUserStats?.liked == true,
                    repostsCount = this.actionPostStats?.reposts ?: 0,
                    userReposted = this.actionPostUserStats?.reposted == true,
                    userBookmarked = false,
                ),
                links = this.actionPostUris.map { it.asEventLinkDO() },
                nostrUris = this.actionPostNostrUris.map { it.asReferencedNostrUriDO() },
                eventZaps = emptyList(),
                bookmark = null,
            )
        },
    )
}
