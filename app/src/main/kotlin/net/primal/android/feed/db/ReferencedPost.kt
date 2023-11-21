package net.primal.android.feed.db

import kotlinx.serialization.Serializable
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.domain.CdnImage

@Serializable
data class ReferencedPost(
    val postId: String,
    val createdAt: Long,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarCdnImage: CdnImage?,
    val authorInternetIdentifier: String?,
    val authorLightningAddress: String?,
    val attachments: List<NoteAttachment>,
    val nostrUris: List<NoteNostrUri>,
)
