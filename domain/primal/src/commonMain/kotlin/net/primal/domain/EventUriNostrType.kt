package net.primal.domain

import kotlinx.serialization.Serializable

@Serializable
enum class EventUriNostrType {
    Zap,
    Note,
    Profile,
    Article,
    Highlight,
    Unsupported,
}
