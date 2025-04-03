package net.primal.android.nostr.mappers

import java.time.Instant
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.parseHashtags
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.domain.ReferencedNote

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
