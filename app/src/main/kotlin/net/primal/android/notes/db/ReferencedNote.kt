package net.primal.android.notes.db

import java.time.Instant
import kotlinx.serialization.Serializable
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.utils.parseHashtags
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.profile.domain.PrimalPremiumInfo

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
    val authorPrimalPremiumInfo: PrimalPremiumInfo?,
    val attachments: List<NoteAttachment>,
    val nostrUris: List<NoteNostrUri>,
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
        authorLegendaryCustomization = this.authorPrimalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
        attachments = this.attachments.map { it.asNoteAttachmentUi() },
        nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() },
        timestamp = Instant.ofEpochSecond(this.createdAt),
        content = this.content,
        stats = EventStatsUi(),
        hashtags = this.content.parseHashtags(),
        rawNostrEventJson = "",
        replyToAuthorHandle = null,
    )
