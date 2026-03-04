package net.primal.data.account.repository.service.model

import net.primal.data.account.signer.remote.model.RemoteSignerMethodResponse

internal data class PendingResponse(
    val response: RemoteSignerMethodResponse,
    val rebroadcast: Boolean = false,
)
