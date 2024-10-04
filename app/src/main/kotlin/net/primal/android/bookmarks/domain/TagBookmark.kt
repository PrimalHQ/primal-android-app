package net.primal.android.bookmarks.domain

import kotlinx.serialization.Serializable

@Serializable
data class TagBookmark(
    val type: String,
    val value: String,
)
