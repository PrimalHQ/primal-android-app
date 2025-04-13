package net.primal.android.notes.feed.model

import java.time.Instant
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.links.CdnImage
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.posts.FeedPost

data class FeedPostUi(
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val timestamp: Instant,
    val content: String,
    val stats: EventStatsUi,
    val rawNostrEventJson: String,
    val rawKind: Int? = rawNostrEventJson.decodeFromJsonStringOrNull<NostrEvent>()?.kind,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val repostAuthorName: String? = null,
    val authorInternetIdentifier: String? = null,
    val authorAvatarCdnImage: CdnImage? = null,
    val uris: List<EventUriUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val replyToAuthorHandle: String? = null,
    val isBookmarked: Boolean = false,
    val eventZaps: List<EventZapUiModel> = emptyList(),
    val authorLegendaryCustomization: LegendaryCustomization? = null,
    val authorBlossoms: List<String> = emptyList(),
)

fun FeedPost.asFeedPostUi(): FeedPostUi {
    val repost = this.reposts.firstOrNull()
    return FeedPostUi(
        postId = this.eventId,
        repostId = repost?.repostId,
        repostAuthorId = repost?.repostAuthorId,
        repostAuthorName = repost?.repostAuthorDisplayName,
        authorId = this.author.authorId,
        authorName = this.author.displayName,
        authorHandle = this.author.handle,
        authorInternetIdentifier = this.author.internetIdentifier?.formatNip05Identifier(),
        authorAvatarCdnImage = this.author.avatarCdnImage,
        timestamp = Instant.ofEpochSecond(this.timestamp.epochSeconds),
        content = this.content,
        uris = this.links.map { it.asEventUriUiModel() }.sortedBy { it.position },
        nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() }.sortedBy { it.position },
        stats = this.stats?.let { stats ->
            EventStatsUi(
                repliesCount = stats.repliesCount,
                userReplied = stats.userReplied,
                zapsCount = stats.zapsCount,
                satsZapped = stats.satsZapped,
                userZapped = stats.userZapped,
                likesCount = stats.likesCount,
                userLiked = stats.userLiked,
                repostsCount = stats.repostsCount,
                userReposted = stats.userReposted,
            )
        } ?: EventStatsUi(),
        hashtags = this.hashtags,
        rawNostrEventJson = this.rawNostrEvent,
        replyToAuthorHandle = this.replyToAuthor?.handle ?: this.replyToAuthor?.authorId?.asEllipsizedNpub(),
        isBookmarked = this.bookmark != null,
        eventZaps = this.eventZaps
            .map { it.asEventZapUiModel() }
            .sortedWith(EventZapUiModel.DefaultComparator),
        authorLegendaryCustomization = this.author.legendProfile?.asLegendaryCustomization(),
        authorBlossoms = this.author.blossomServers,
    )
}
