package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.AppPermissionData
import net.primal.data.account.local.dao.PendingNostrEvent
import net.primal.data.account.local.dao.PermissionAction
import net.primal.data.account.local.dao.RequestState
import net.primal.data.account.local.dao.TrustLevel
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.asDomain
import net.primal.data.account.repository.mappers.getRequestTypeId
import net.primal.domain.account.handler.Nip46EventsHandler
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

class SessionEventRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
    private val nip46EventsHandler: Nip46EventsHandler,
) : SessionEventRepository {

    override fun observeEventsPendingUserAction(signerPubKey: String): Flow<List<SessionEvent>> =
        database.sessionEvents().observeEventsByRequestState(
            signerPubKey = signerPubKey.asEncryptable(),
            requestState = RequestState.PendingUserAction,
        ).map { events -> events.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeCompletedEventsForSession(sessionId: String): Flow<List<SessionEvent>> {
        return database.sessionEvents().observeCompletedEventsBySessionId(sessionId = sessionId)
            .map { list -> list.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()
    }

    override fun observeEvent(eventId: String): Flow<SessionEvent?> {
        return database.sessionEvents().observeEvent(eventId = eventId)
            .map { it?.asDomain() }
            .distinctUntilChanged()
    }

    override suspend fun processMissedEvents(signerKeyPair: NostrKeyPair, eventIds: List<String>): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                val events = nip46EventsHandler.fetchNip46Events(eventIds = eventIds).getOrThrow()

                database.pendingNostrEvents().upsertAll(
                    data = events.map {
                        PendingNostrEvent(
                            eventId = it.id,
                            clientPubKey = it.pubKey,
                            signerPubKey = signerKeyPair.pubKey.asEncryptable(),
                            rawNostrEventJson = it.encodeToJsonString().asEncryptable(),
                        )
                    },
                )
            }
        }

    override suspend fun respondToEvent(eventId: String, userChoice: UserChoice): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                when (userChoice) {
                    UserChoice.Allow -> allowEvent(eventId = eventId)

                    UserChoice.Reject -> rejectEvent(eventId = eventId)

                    UserChoice.AlwaysAllow -> {
                        allowEvent(eventId = eventId)
                        updatePermissionPreference(eventId = eventId, action = PermissionAction.Approve)
                    }

                    UserChoice.AlwaysReject -> {
                        rejectEvent(eventId = eventId)
                        updatePermissionPreference(eventId = eventId, action = PermissionAction.Deny)
                    }
                }
            }
        }

    override suspend fun respondToEvents(userChoices: List<SessionEventUserChoice>): Result<Unit> =
        withContext(dispatchers.io()) {
            database.withTransaction {
                runCatching {
                    userChoices.forEach {
                        respondToEvent(eventId = it.sessionEventId, userChoice = it.userChoice).getOrThrow()
                    }
                }
            }
        }

    private suspend fun updatePermissionPreference(eventId: String, action: PermissionAction) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                val sessionEvent = database.sessionEvents().getSessionEvent(eventId = eventId) ?: return@withTransaction
                val connection = database.connections().getConnection(clientPubKey = sessionEvent.clientPubKey)
                    ?: return@withTransaction

                if (connection.data.trustLevel == TrustLevel.Low && action == PermissionAction.Approve) {
                    database.connections().updateTrustLevel(
                        clientPubKey = connection.data.clientPubKey,
                        trustLevel = TrustLevel.Medium,
                    )
                    database.permissions().deletePermissions(clientPubKey = connection.data.clientPubKey)
                }

                database.permissions().upsert(
                    data = AppPermissionData(
                        permissionId = sessionEvent.getRequestTypeId(),
                        clientPubKey = connection.data.clientPubKey,
                        action = action,
                    ),
                )
            }
        }

    private suspend fun allowEvent(eventId: String) =
        withContext(dispatchers.io()) {
            database.sessionEvents().updateSessionEventRequestState(
                eventId = eventId,
                requestState = RequestState.PendingResponse,
                responsePayload = null,
                completedAt = null,
            )
        }

    private suspend fun rejectEvent(eventId: String) =
        withContext(dispatchers.io()) {
            val clientPubKey = database.sessionEvents().getSessionEvent(eventId = eventId)?.clientPubKey
            if (clientPubKey != null) {
                database.sessionEvents().updateSessionEventRequestState(
                    eventId = eventId,
                    requestState = RequestState.PendingResponse,
                    completedAt = null,
                    responsePayload = RemoteSignerMethodResponse.Error(
                        id = eventId,
                        clientPubKey = clientPubKey,
                        error = "User rejected this request.",
                    ).encodeToJsonString().asEncryptable(),
                )
            }
        }
}
