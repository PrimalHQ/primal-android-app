package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.events.EventUri
import net.primal.domain.links.EventLink

internal fun EventUri.asEventLinkDO(forcePosition: Int? = null): EventLink {
    return EventLink(
        eventId = this.eventId,
        position = forcePosition ?: this.position,
        url = this.url,
        type = this.type,
        mimeType = this.mimeType,
        variants = this.variants,
        title = this.title,
        description = this.description,
        thumbnail = this.thumbnail,
        authorAvatarUrl = this.authorAvatarUrl,
    )
}
