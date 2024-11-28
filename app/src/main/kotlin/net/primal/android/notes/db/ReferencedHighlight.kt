package net.primal.android.notes.db

import kotlinx.serialization.Serializable

@Serializable
data class ReferencedHighlight(
    val text: String,
    val aTag: String,
    val authorId: String?,
)
