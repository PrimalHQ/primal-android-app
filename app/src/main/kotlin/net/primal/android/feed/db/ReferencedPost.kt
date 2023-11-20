package net.primal.android.feed.db

import kotlinx.serialization.Serializable
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.domain.CdnResourceVariant

@Serializable
data class ReferencedPost(
    val postId: String,
    val createdAt: Long,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val authorAvatarVariants: List<CdnResourceVariant>,
    val authorInternetIdentifier: String?,
    val authorLightningAddress: String?,
    val nostrResources: List<NoteNostrUri>,
)
