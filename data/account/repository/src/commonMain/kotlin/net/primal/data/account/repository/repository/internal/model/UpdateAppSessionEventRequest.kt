package net.primal.data.account.repository.repository.internal.model

import net.primal.data.account.local.dao.apps.AppRequestState

internal data class UpdateAppSessionEventRequest(
    val eventId: String,
    val requestState: AppRequestState,
    val responsePayload: String?,
    val completedAt: Long?,
)
