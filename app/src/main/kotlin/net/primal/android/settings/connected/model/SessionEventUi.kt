package net.primal.android.settings.connected.model

import net.primal.domain.account.model.SessionEvent

data class SessionEventUi(
    val id: String,
    val requestTypeId: String,
    val timestamp: Long,
)

fun SessionEvent.asSessionEventUi(): SessionEventUi {
    return SessionEventUi(
        id = this.eventId,
        requestTypeId = this.requestTypeId,
        timestamp = this.requestedAt,
    )
}
