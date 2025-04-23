package net.primal.domain.posts

data class FeedPostRepostInfo(
    val repostId: String,
    val repostAuthorId: String?,
    val repostAuthorDisplayName: String?,
    val repostTimestamp: Long?,
)
