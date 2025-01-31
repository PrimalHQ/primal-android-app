package net.primal.android.notes.feed.model

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.notes.db.FeedPost
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.stats.ui.EventZapUiModel
import net.primal.android.stats.ui.asEventZapUiModel

data class FeedPostUi(
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val timestamp: Instant,
    val content: String,
    val stats: EventStatsUi,
    val rawNostrEventJson: String,
    val rawKind: Int? = NostrJson.decodeFromStringOrNull<NostrEvent>(rawNostrEventJson)?.kind,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val repostAuthorName: String? = null,
    val authorInternetIdentifier: String? = null,
    val authorAvatarCdnImage: CdnImage? = null,
    val attachments: List<NoteAttachmentUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val replyToAuthorHandle: String? = null,
    val isBookmarked: Boolean = false,
    val eventZaps: List<EventZapUiModel> = emptyList(),
    val authorLegendaryCustomization: LegendaryCustomization? = null,
    val authorBlossoms: List<String> = emptyList(),
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
        attachments = this.attachments.map { it.asNoteAttachmentUi() },
        nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() },
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
    )
}
