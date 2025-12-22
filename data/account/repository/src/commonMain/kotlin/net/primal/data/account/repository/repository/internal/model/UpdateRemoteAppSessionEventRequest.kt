package net.primal.data.account.repository.repository.internal.model

import net.primal.data.account.local.dao.apps.remote.RemoteAppRequestState

internal data class UpdateRemoteAppSessionEventRequest(
    val eventId: String,
    val requestState: RemoteAppRequestState,
    val responsePayload: String?,
    val completedAt: Long?,
)
