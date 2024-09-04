package net.primal.android.notes.db

import kotlinx.serialization.Serializable

@Serializable
data class ReferencedUser(
    val userId: String,
    val handle: String,
) {
    val displayUsername get() = "@$handle"
}
