package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.events.EventRelayHints as EventRelayHintsPO
import net.primal.domain.model.EventRelayHints as EventRelayHintsDO

fun EventRelayHintsPO.asEventRelayHintsDO(): EventRelayHintsDO {
    return EventRelayHintsDO(
        eventId = this.eventId,
        relays = this.relays,
    )
}
