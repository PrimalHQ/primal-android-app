package net.primal.data.repository.mapper

import kotlinx.datetime.Instant
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.notes.FeedPost as FeedPostPO
import net.primal.data.local.dao.notes.FeedPostUserStats
import net.primal.data.utils.authorNameUiFriendly
import net.primal.data.utils.usernameUiFriendly
import net.primal.domain.EventZap
import net.primal.domain.common.utils.asEllipsizedNpub
import net.primal.domain.common.utils.formatNip05Identifier
import net.primal.domain.model.FeedPost as FeedPostDO
import net.primal.domain.model.FeedPostAuthor
import net.primal.domain.model.FeedPostRepostInfo
import net.primal.domain.model.FeedPostStats

internal fun FeedPostPO.mapAsFeedPostDO(): FeedPostDO {
    return FeedPostDO(
        eventId = this.data.postId,
        author = FeedPostAuthor(
            authorId = this.data.authorId,
            handle = this.author?.usernameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
            displayName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
            internetIdentifier = this.author?.internetIdentifier?.formatNip05Identifier(),
            avatarCdnImage = this.author?.avatarCdnImage,
            legendProfile = this.author?.primalPremiumInfo?.legendProfile,
            blossomServers = this.author?.blossoms ?: emptyList(),
        ),
        content = this.data.content,
        timestamp = Instant.fromEpochSeconds(this.data.createdAt),
        rawNostrEvent = this.data.raw,
        hashtags = this.data.hashtags,
        replyToAuthor = this.replyToAuthor?.let { replyToAuthor ->
            FeedPostAuthor(
                authorId = replyToAuthor.ownerId,
                handle = replyToAuthor.usernameUiFriendly(),
                displayName = replyToAuthor.authorNameUiFriendly(),
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
    )
}

private fun buildFeedPostStats(eventStats: EventStats?, feedPostUserStats: FeedPostUserStats?) =
    FeedPostStats(
        repliesCount = eventStats?.replies ?: 0,
        userReplied = feedPostUserStats?.userReplied ?: false,
        zapsCount = eventStats?.zaps ?: 0,
        satsZapped = eventStats?.satsZapped ?: 0,
        userZapped = feedPostUserStats?.userZapped ?: false,
        likesCount = eventStats?.likes ?: 0,
        userLiked = feedPostUserStats?.userLiked ?: false,
        repostsCount = eventStats?.reposts ?: 0,
        userReposted = feedPostUserStats?.userReposted ?: false,
    )

private fun buildFeedPostStats(eventStats: EventStats?, userStats: EventUserStats?) =
    FeedPostStats(
        repliesCount = eventStats?.replies ?: 0,
        userReplied = userStats?.replied ?: false,
        zapsCount = eventStats?.zaps ?: 0,
        satsZapped = eventStats?.satsZapped ?: 0,
        userZapped = userStats?.zapped ?: false,
        likesCount = eventStats?.likes ?: 0,
        userLiked = userStats?.liked ?: false,
        repostsCount = eventStats?.reposts ?: 0,
        userReposted = userStats?.reposted ?: false,
    )
