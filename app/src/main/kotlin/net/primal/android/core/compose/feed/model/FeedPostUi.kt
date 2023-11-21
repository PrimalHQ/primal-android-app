package net.primal.android.core.compose.feed.model

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.feed.db.FeedPost

data class FeedPostUi(
    val postId: String,
    val repostId: String? = null,
    val repostAuthorId: String? = null,
    val repostAuthorName: String? = null,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorInternetIdentifier: String? = null,
    val authorAvatarCdnImage: CdnImage? = null,
    val attachments: List<NoteAttachmentUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val timestamp: Instant,
    val content: String,
    val stats: FeedPostStatsUi,
    val hashtags: List<String> = emptyList(),
    val rawNostrEventJson: String,
)

fun FeedPost.asFeedPostUi() =
    FeedPostUi(
        postId = this.data.postId,
        repostId = this.data.repostId,
        repostAuthorId = this.data.repostAuthorId,
        repostAuthorName = this.repostAuthor?.authorNameUiFriendly() ?: this.data.repostAuthorId?.asEllipsizedNpub(),
        authorId = this.author?.ownerId ?: this.data.authorId,
        authorName = this.author?.authorNameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        authorHandle = this.author?.usernameUiFriendly() ?: this.data.authorId.asEllipsizedNpub(),
        authorInternetIdentifier = this.author?.internetIdentifier,
        authorAvatarCdnImage = this.author?.avatarCdnImage,
        timestamp = Instant.ofEpochSecond(this.data.createdAt),
        content = this.data.content,
        attachments = this.attachments.map { it.asNoteAttachmentUi() },
        nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() },
        stats = FeedPostStatsUi.from(postStats = this.postStats, userStats = this.userStats),
        hashtags = this.data.hashtags,
        rawNostrEventJson = this.data.raw,
    )
