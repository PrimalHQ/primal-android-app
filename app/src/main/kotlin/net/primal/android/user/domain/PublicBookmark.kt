package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class PublicBookmark(
    val type: String,
    val value: String,
)
