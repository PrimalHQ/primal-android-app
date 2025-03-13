package net.primal.android.notes.db

import java.time.Instant
import kotlinx.serialization.Serializable
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.parseHashtags
import net.primal.android.events.db.EventUri
import net.primal.android.events.db.EventUriNostr
import net.primal.android.events.domain.CdnImage
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.profile.domain.PrimalLegendProfile

@Serializable
data class ReferencedNote(
    val postId: String,
    val createdAt: Long,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarCdnImage: CdnImage?,
    val authorInternetIdentifier: String?,
    val authorLightningAddress: String?,
    val authorLegendProfile: PrimalLegendProfile?,
    val attachments: List<EventUri>,
    val nostrUris: List<EventUriNostr>,
    val raw: String,
)

fun ReferencedNote.asFeedPostUi() =
    FeedPostUi(
        postId = this.postId,
        repostId = null,
        repostAuthorId = null,
        repostAuthorName = null,
        authorId = this.authorId,
        authorName = this.authorName,
        authorHandle = this.authorName,
        authorInternetIdentifier = this.authorInternetIdentifier,
        authorAvatarCdnImage = this.authorAvatarCdnImage,
        authorLegendaryCustomization = this.authorLegendProfile?.asLegendaryCustomization(),
        uris = this.attachments.map { it.asEventUriUiModel() },
        nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() },
        timestamp = Instant.ofEpochSecond(this.createdAt),
        content = this.content,
        stats = EventStatsUi(),
        hashtags = this.content.parseHashtags(),
        rawNostrEventJson = this.raw,
        replyToAuthorHandle = null,
    )
