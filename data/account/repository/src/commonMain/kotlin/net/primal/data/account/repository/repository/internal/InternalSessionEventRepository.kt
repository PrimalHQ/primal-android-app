package net.primal.data.account.repository.repository.internal

import kotlin.time.Clock
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.local.dao.apps.SignerMethodType
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.signer.model.RemoteSignerMethod
import net.primal.data.account.remote.signer.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.buildSessionEventData
import net.primal.data.account.repository.repository.internal.model.UpdateAppSessionEventRequest
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
        requestType: SignerMethodType,
        method: RemoteSignerMethod?,
        response: RemoteSignerMethodResponse?,
        requestState: AppRequestState? = null,
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
        requestType: SignerMethodType,
        method: LocalSignerMethod?,
        response: LocalSignerMethodResponse?,
        requestState: AppRequestState? = null,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val completedAt = Clock.System.now().epochSeconds
            buildSessionEventData(
                sessionId = sessionId,
                requestedAt = method?.requestedAt ?: completedAt,
                completedAt = if (response != null) completedAt else null,
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
        accountDatabase.remoteAppSessionEvents().observeEventsBySignerAndRequestState(
            signerPubKey = signerPubKey,
            requestState = AppRequestState.PendingResponse,
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

    suspend fun updateRemoteAppSessionEventState(requests: List<UpdateAppSessionEventRequest>) =
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

    suspend fun updateLocalAppSessionEventState(requests: List<UpdateAppSessionEventRequest>) =
        withContext(dispatchers.io()) {
            accountDatabase.withTransaction {
                requests.forEach { request ->
                    accountDatabase.localAppSessionEvents().updateSessionEventRequestState(
                        eventId = request.eventId,
                        requestState = request.requestState,
                        responsePayload = request.responsePayload?.asEncryptable(),
                        completedAt = request.completedAt,
                    )
                }
            }
        }
}
