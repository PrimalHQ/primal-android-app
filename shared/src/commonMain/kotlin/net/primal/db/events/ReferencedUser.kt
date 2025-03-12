package net.primal.db.events

import kotlinx.serialization.Serializable

@Serializable
data class ReferencedUser(
    val userId: String,
    val handle: String,
) {
    val displayUsername get() = "@$handle"
}
