package net.primal.data.account.repository.repository.model

import net.primal.data.account.local.dao.RequestState

internal data class UpdateSessionEventRequest(
    val eventId: String,
    val requestState: RequestState,
    val responsePayload: String?,
)
