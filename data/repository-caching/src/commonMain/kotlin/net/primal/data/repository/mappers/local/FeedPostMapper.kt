package net.primal.data.repository.mappers.local

import kotlinx.datetime.Instant
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.notes.FeedPost as FeedPostPO
import net.primal.data.local.dao.notes.FeedPostUserStats
import net.primal.data.local.dao.notes.PostData
import net.primal.data.repository.mappers.authorNameUiFriendly
import net.primal.data.repository.mappers.usernameUiFriendly
import net.primal.domain.events.EventZap
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.nostr.utils.formatNip05Identifier
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedPost as FeedPostDO
import net.primal.domain.posts.FeedPostAuthor
import net.primal.domain.posts.FeedPostRepostInfo
import net.primal.domain.posts.FeedPostStats

internal fun PostData.mapAsFeedPostDO(): FeedPost {
    return FeedPost(
        eventId = this.postId,
        author = FeedPostAuthor(
            authorId = this.authorId,
            handle = this.authorId.asEllipsizedNpub(),
            displayName = this.authorId.asEllipsizedNpub(),
        ),
        content = this.content,
        tags = this.tags,
        timestamp = Instant.fromEpochSeconds(this.createdAt),
        rawNostrEvent = this.raw,
        hashtags = this.hashtags,
        replyToAuthor = this.replyToAuthorId?.let { replyToAuthorId ->
            FeedPostAuthor(
                authorId = replyToAuthorId,
                handle = replyToAuthorId.asEllipsizedNpub(),
                displayName = replyToAuthorId.asEllipsizedNpub(),
            )
        },
    )
}

internal fun FeedPostPO.mapAsFeedPostDO(): FeedPostDO {
    return FeedPostDO(
        eventId = this.data.postId,
        author = FeedPostAuthor(
            authorId = this.data.authorId,
            handle = this.author?.usernameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
            displayName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
            rawNostrEvent = this.author?.raw,
            internetIdentifier = this.author?.internetIdentifier?.formatNip05Identifier(),
            avatarCdnImage = this.author?.avatarCdnImage,
            legendProfile = this.author?.primalPremiumInfo?.legendProfile,
            blossomServers = this.author?.blossoms ?: emptyList(),
        ),
        content = this.data.content,
        tags = this.data.tags,
        timestamp = Instant.fromEpochSeconds(this.data.createdAt),
        rawNostrEvent = this.data.raw,
        hashtags = this.data.hashtags,
        replyToAuthor = this.replyToAuthor?.let { replyToAuthor ->
            FeedPostAuthor(
                authorId = replyToAuthor.ownerId,
                handle = replyToAuthor.usernameUiFriendly(),
                displayName = replyToAuthor.authorNameUiFriendly(),
                rawNostrEvent = replyToAuthor.raw,
                internetIdentifier = replyToAuthor.internetIdentifier?.formatNip05Identifier(),
                avatarCdnImage = replyToAuthor.avatarCdnImage,
                legendProfile = replyToAuthor.primalPremiumInfo?.legendProfile,
                blossomServers = replyToAuthor.blossoms,
            )
        },
        reposts = this.data.repostId?.let { repostId ->
            listOf(
                FeedPostRepostInfo(
                    repostId = repostId,
                    repostAuthorId = this.data.repostAuthorId,
                    repostAuthorDisplayName = this.repostAuthor?.authorNameUiFriendly()
                        ?: this.data.repostAuthorId?.asEllipsizedNpub(),
                ),
            )
        } ?: emptyList(),
        stats = if (this.eventStats != null || this.userStats != null) {
            buildFeedPostStats(
                eventStats = this.eventStats,
                feedPostUserStats = this.userStats,
            )
        } else {
            null
        },
        links = this.uris.sortedBy { it.position }.mapIndexed { index, eventUri ->
            eventUri.asEventLinkDO(forcePosition = index)
        },
        nostrUris = this.nostrUris.sortedBy { it.position }.mapIndexed { index, eventUriNostr ->
            eventUriNostr.asReferencedNostrUriDO(forcePosition = index)
        },
        eventZaps = this.eventZaps.map { it.asEventZapDO() }.sortedWith(EventZap.DefaultComparator),
        bookmark = this.bookmark?.asPublicBookmark(),
        isThreadMuted = this.data.isThreadMuted == true,
        eventRelayHints = this.eventRelayHints?.asEventRelayHintsDO(),
    )
}

private fun buildFeedPostStats(eventStats: EventStats?, feedPostUserStats: FeedPostUserStats?) =
    FeedPostStats(
        repliesCount = eventStats?.replies ?: 0,
        userReplied = feedPostUserStats?.userReplied == true,
        zapsCount = eventStats?.zaps ?: 0,
        satsZapped = eventStats?.satsZapped ?: 0,
        userZapped = feedPostUserStats?.userZapped == true,
        likesCount = eventStats?.likes ?: 0,
        userLiked = feedPostUserStats?.userLiked == true,
        repostsCount = eventStats?.reposts ?: 0,
        userReposted = feedPostUserStats?.userReposted == true,
    )
