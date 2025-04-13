package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.events.EventUriNostr
import net.primal.domain.links.EventUriNostrReference

internal fun EventUriNostr.asReferencedNostrUriDO(forcePosition: Int? = null): EventUriNostrReference {
    return EventUriNostrReference(
        eventId = this.eventId,
        position = forcePosition ?: this.position,
        uri = this.uri,
        type = this.type,
        referencedEventAlt = this.referencedEventAlt,
        referencedHighlight = this.referencedHighlight,
        referencedNote = this.referencedNote,
        referencedArticle = this.referencedArticle,
        referencedUser = this.referencedUser,
        referencedZap = this.referencedZap,
    )
}
