package net.primal.android.feed.db

data class ReferencedPost(
    val postId: String,
    val createdAt: Long,
    val content: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val authorInternetIdentifier: String?,
)
