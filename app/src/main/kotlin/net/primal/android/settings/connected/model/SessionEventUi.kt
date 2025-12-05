package net.primal.android.settings.connected.model

import net.primal.domain.account.model.RequestState
import net.primal.domain.account.model.SessionEvent

data class SessionEventUi(
    val id: String,
    val requestTypeId: String,
    val requestState: RequestState,
    val timestamp: Long,
)

fun SessionEvent.asSessionEventUi(): SessionEventUi {
    return SessionEventUi(
        id = this.eventId,
        requestState = this.requestState,
        requestTypeId = this.requestTypeId,
        timestamp = this.requestedAt,
    )
}
