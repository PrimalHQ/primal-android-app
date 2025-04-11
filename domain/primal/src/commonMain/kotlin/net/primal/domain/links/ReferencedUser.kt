package net.primal.domain.links

import kotlinx.serialization.Serializable

@Serializable
data class ReferencedUser(
    val userId: String,
    val handle: String,
) {
    val displayUsername get() = "@$handle"
}
