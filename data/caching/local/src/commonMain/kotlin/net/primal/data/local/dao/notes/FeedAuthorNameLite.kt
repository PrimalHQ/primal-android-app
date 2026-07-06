package net.primal.data.local.dao.notes

/**
 * Name-only column-subset of [net.primal.data.local.dao.profiles.ProfileData]
 * for the feed `repostAuthor` / `replyToAuthor` relations, which are only
 * ever rendered as a name.
 */
data class FeedAuthorNameLite(
    val ownerId: String,
    val displayName: String? = null,
    val handle: String? = null,
)
