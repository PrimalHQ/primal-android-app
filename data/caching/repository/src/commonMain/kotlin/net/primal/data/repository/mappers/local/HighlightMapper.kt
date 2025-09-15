package net.primal.data.repository.mappers.local

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import net.primal.data.local.dao.reads.Highlight as HighlightPO
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.posts.FeedPost
import net.primal.domain.posts.FeedPostAuthor
import net.primal.domain.reads.Highlight as HighlightDO
import net.primal.domain.reads.HighlightData

@OptIn(ExperimentalTime::class)
fun HighlightPO.asHighlightDO(): HighlightDO {
    return HighlightDO(
        data = HighlightData(
            highlightId = this.data.highlightId,
            authorId = this.data.authorId,
            content = this.data.content,
            context = this.data.context,
            alt = this.data.alt,
            referencedEventATag = this.data.referencedEventATag,
            referencedEventAuthorId = this.data.referencedEventAuthorId,
            createdAt = this.data.createdAt,
        ),
        author = this.author?.asProfileDataDO(),
        comments = this.comments.map {
            FeedPost(
                eventId = it.post.postId,
                author = FeedPostAuthor(
                    authorId = it.post.authorId,
                    handle = it.author?.handle ?: it.post.authorId.asEllipsizedNpub(),
                    displayName = it.author?.displayName ?: it.post.authorId.asEllipsizedNpub(),
                    rawNostrEvent = it.author?.raw,
                    internetIdentifier = it.author?.internetIdentifier,
                    avatarCdnImage = it.author?.avatarCdnImage,
                    legendProfile = it.author?.primalPremiumInfo?.legendProfile,
                    blossomServers = it.author?.blossoms ?: emptyList(),
                ),
                content = it.post.content,
                tags = it.post.tags,
                timestamp = Instant.fromEpochSeconds(it.post.createdAt),
                rawNostrEvent = it.post.raw,
                hashtags = it.post.hashtags,
            )
        },
    )
}
