package net.primal.data.account.repository.repository

import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse

internal interface InternalSessionEventRepository {
    suspend fun saveSessionEvent(
        sessionId: String,
        requestedAt: Long,
        method: RemoteSignerMethod,
        response: RemoteSignerMethodResponse,
    )
}
