package net.primal.android.feed.db

import kotlinx.serialization.Serializable


@Serializable
data class ReferencedPost(
    val postId: String,
    val createdAt: Long,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val authorInternetIdentifier: String?,
    val authorLightningAddress: String?,
    val mediaResources: List<MediaResource>,
    val nostrResources: List<NostrResource>,
)
