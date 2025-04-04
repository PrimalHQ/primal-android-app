package net.primal.android.notes.feed.model

import java.time.Instant
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.nostr.db.EventRelayHints
import net.primal.android.notes.db.FeedPost
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.CdnImage
import net.primal.domain.nostr.MAX_RELAY_HINTS
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV.toNeventString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

data class FeedPostUi(
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val timestamp: Instant,
    val content: String,
    val stats: EventStatsUi,
    val rawNostrEventJson: String,
    val rawKind: Int? = CommonJson.decodeFromStringOrNull<NostrEvent>(rawNostrEventJson)?.kind,
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
    val eventRelayHints: EventRelayHints? = null,
)

fun FeedPost.asFeedPostUi(): FeedPostUi {
    return FeedPostUi(
        postId = this.data.postId,
        repostId = this.data.repostId,
        repostAuthorId = this.data.repostAuthorId,
        repostAuthorName = this.repostAuthor?.authorNameUiFriendly() ?: this.data.repostAuthorId?.asEllipsizedNpub(),
        authorId = this.author?.ownerId ?: this.data.authorId,
        authorName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        authorHandle = this.author?.usernameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        authorInternetIdentifier = this.author?.internetIdentifier?.formatNip05Identifier(),
        authorAvatarCdnImage = this.author?.avatarCdnImage,
        timestamp = Instant.ofEpochSecond(this.data.createdAt),
        content = this.data.content,
        uris = this.uris.map { it.asEventUriUiModel() }.sortedBy { it.position },
        nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() }.sortedBy { it.position },
        stats = EventStatsUi.from(eventStats = this.eventStats, feedPostUserStats = this.userStats),
        hashtags = this.data.hashtags,
        rawNostrEventJson = this.data.raw,
        replyToAuthorHandle = this.replyToAuthor?.usernameUiFriendly() ?: this.data.replyToAuthorId?.asEllipsizedNpub(),
        isBookmarked = this.bookmark != null,
        eventZaps = this.eventZaps
            .map { it.asEventZapUiModel() }
            .sortedWith(EventZapUiModel.DefaultComparator),
        authorLegendaryCustomization = this.author?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
        authorBlossoms = this.author?.blossoms ?: emptyList(),
        eventRelayHints = this.eventRelayHints,
    )
}

fun FeedPostUi.asNeventString(): String {
    return Nevent(
        eventId = this.postId,
        kind = NostrEventKind.ShortTextNote.value,
        userId = this.authorId,
        relays = this.eventRelayHints?.relays?.take(MAX_RELAY_HINTS) ?: emptyList(),
    ).toNeventString()
}
