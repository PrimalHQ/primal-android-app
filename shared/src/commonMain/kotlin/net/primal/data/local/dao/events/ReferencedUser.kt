package net.primal.data.local.dao.events

import kotlinx.serialization.Serializable

@Serializable
data class ReferencedUser(
    val userId: String,
    val handle: String,
) {
    val displayUsername get() = "@$handle"
}
