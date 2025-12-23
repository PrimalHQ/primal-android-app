package net.primal.data.account.repository.repository.internal

import kotlin.time.Clock
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.local.dao.apps.local.LocalRequestState
import net.primal.data.account.local.dao.apps.local.LocalSignerMethodType
import net.primal.data.account.local.dao.apps.remote.RemoteAppRequestState
import net.primal.data.account.local.dao.apps.remote.RemoteSignerMethodType
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.buildSessionEventData
import net.primal.data.account.repository.repository.internal.model.UpdateRemoteAppSessionEventRequest
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.nostr.NostrEvent
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

internal class InternalSessionEventRepository(
    private val accountDatabase: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun saveRemoteAppSessionEvent(
        sessionId: String,
        signerPubKey: String,
        requestType: RemoteSignerMethodType,
        method: RemoteSignerMethod?,
        response: RemoteSignerMethodResponse?,
        requestState: RemoteAppRequestState? = null,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val completedAt = Clock.System.now().epochSeconds

            buildSessionEventData(
                sessionId = sessionId,
                signerPubKey = signerPubKey,
                requestedAt = method?.requestedAt ?: completedAt,
                completedAt = if (response != null) completedAt else null,
                method = method,
                requestType = requestType,
                response = response,
                requestState = requestState,
            )?.let { sessionEventData ->
                accountDatabase.remoteAppSessionEvents().insert(data = sessionEventData)
            } ?: throw IllegalArgumentException("Couldn't build session event data.")
        }
    }

    suspend fun saveLocalSessionEvent(
        sessionId: String,
        requestType: LocalSignerMethodType,
        method: LocalSignerMethod?,
        response: LocalSignerMethodResponse?,
        requestState: LocalRequestState? = null,
    ) = withContext(dispatchers.io()) {
        runCatching {
            buildSessionEventData(
                sessionId = sessionId,
                processedAt = Clock.System.now().epochSeconds,
                requestType = requestType,
                method = method,
                response = response,
                requestState = requestState,
            )?.let { sessionEventData ->
                accountDatabase.localAppSessionEvents().insert(data = sessionEventData)
            } ?: throw IllegalArgumentException("Couldn't build session event data.")
        }
    }

    fun observeRemoteAppPendingResponseEvents(signerPubKey: String) =
        accountDatabase.remoteAppSessionEvents().observeEventsByRequestState(
            signerPubKey = signerPubKey,
            requestState = RemoteAppRequestState.PendingResponse,
        ).distinctUntilChanged()

    fun observeRemoteAppPendingNostrEvents(signerPubKey: String) =
        accountDatabase.remoteAppPendingNostrEvents().observeAllBySignerPubKey(
            signerPubKey = signerPubKey,
        ).map { list -> list.mapNotNull { it.rawNostrEventJson.decrypted.decodeFromJsonStringOrNull<NostrEvent>() } }
            .distinctUntilChanged()

    suspend fun deleteRemoteAppPendingNostrEvents(eventIds: List<String>) =
        withContext(dispatchers.io()) {
            runCatching {
                accountDatabase.remoteAppPendingNostrEvents().deleteByIds(eventIds = eventIds)
            }
        }

    suspend fun updateRemoteAppSessionEventState(requests: List<UpdateRemoteAppSessionEventRequest>) =
        withContext(dispatchers.io()) {
            accountDatabase.withTransaction {
                requests.forEach { request ->
                    accountDatabase.remoteAppSessionEvents().updateSessionEventRequestState(
                        eventId = request.eventId,
                        requestState = request.requestState,
                        responsePayload = request.responsePayload?.asEncryptable(),
                        completedAt = request.completedAt,
                    )
                }
            }
        }
}
