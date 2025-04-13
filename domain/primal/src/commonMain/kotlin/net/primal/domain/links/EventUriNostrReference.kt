package net.primal.domain.links

import kotlinx.serialization.Serializable

@Serializable
data class EventUriNostrReference(
    val eventId: String,
    val uri: String,
    val type: EventUriNostrType,
    val position: Int? = null,
    val referencedEventAlt: String? = null,
    val referencedHighlight: ReferencedHighlight? = null,
    val referencedNote: ReferencedNote? = null,
    val referencedArticle: ReferencedArticle? = null,
    val referencedUser: ReferencedUser? = null,
    val referencedZap: ReferencedZap? = null,
)
