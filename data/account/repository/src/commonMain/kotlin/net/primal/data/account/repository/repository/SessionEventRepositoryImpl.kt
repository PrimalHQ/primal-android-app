package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.apps.AppPermissionData
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.local.dao.apps.PermissionAction
import net.primal.data.account.local.dao.apps.TrustLevel
import net.primal.data.account.local.dao.apps.remote.RemoteAppPendingNostrEvent
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.signer.model.RemoteSignerMethodResponse
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

    override fun observeEventsPendingUserActionForRemoteSigner(signerPubKey: String): Flow<List<SessionEvent>> =
        database.remoteAppSessionEvents().observeEventsBySignerAndRequestState(
            signerPubKey = signerPubKey,
            requestState = AppRequestState.PendingUserAction,
        ).map { events -> events.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeCompletedEventsForRemoteSession(sessionId: String): Flow<List<SessionEvent>> {
        return database.remoteAppSessionEvents().observeCompletedEventsBySessionId(sessionId = sessionId)
            .map { list -> list.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()
    }

    override fun observeEventsPendingUserActionForLocalApp(appIdentifier: String): Flow<List<SessionEvent>> =
        database.localAppSessionEvents().observeEventsByAppIdentifierAndRequestState(
            appIdentifier = appIdentifier,
            requestState = AppRequestState.PendingUserAction,
        ).map { events -> events.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeCompletedEventsForLocalSession(sessionId: String): Flow<List<SessionEvent>> {
        return database.localAppSessionEvents().observeCompletedEventsBySessionId(sessionId = sessionId)
            .map { list -> list.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()
    }

    override fun observeRemoteEvent(eventId: String): Flow<SessionEvent?> {
        return database.remoteAppSessionEvents().observeEvent(eventId = eventId)
            .map { it?.asDomain() }
            .distinctUntilChanged()
    }

    override fun observeLocalEvent(eventId: String): Flow<SessionEvent?> {
        return database.localAppSessionEvents().observeEvent(eventId = eventId)
            .map { it?.asDomain() }
            .distinctUntilChanged()
    }

    override suspend fun notifyMissedNostrEvents(signerKeyPair: NostrKeyPair, eventIds: List<String>): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                val events = nip46EventsHandler.fetchNip46Events(eventIds = eventIds).getOrThrow()

                database.remoteAppPendingNostrEvents().upsertAll(
                    data = events.map {
                        RemoteAppPendingNostrEvent(
                            eventId = it.id,
                            clientPubKey = it.pubKey,
                            signerPubKey = signerKeyPair.pubKey,
                            rawNostrEventJson = it.encodeToJsonString().asEncryptable(),
                        )
                    },
                )
            }
        }

    override suspend fun respondToRemoteEvent(eventId: String, userChoice: UserChoice): Result<Unit> =
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

    override suspend fun respondToRemoteEvents(userChoices: List<SessionEventUserChoice>): Result<Unit> =
        withContext(dispatchers.io()) {
            database.withTransaction {
                runCatching {
                    userChoices.forEach {
                        respondToRemoteEvent(eventId = it.sessionEventId, userChoice = it.userChoice).getOrThrow()
                    }
                }
            }
        }

    private suspend fun updatePermissionPreference(eventId: String, action: PermissionAction) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                val sessionEvent = database.remoteAppSessionEvents().getSessionEvent(
                    eventId = eventId,
                ) ?: return@withTransaction

                val connection = database.remoteAppConnections().getConnection(
                    clientPubKey = sessionEvent.clientPubKey,
                ) ?: return@withTransaction

                if (connection.data.trustLevel == TrustLevel.Low && action == PermissionAction.Approve) {
                    database.remoteAppConnections().updateTrustLevel(
                        clientPubKey = connection.data.clientPubKey,
                        trustLevel = TrustLevel.Medium,
                    )
                    database.appPermissions().deletePermissions(appIdentifier = connection.data.clientPubKey)
                }

                database.appPermissions().upsert(
                    data = AppPermissionData(
                        permissionId = sessionEvent.getRequestTypeId(),
                        appIdentifier = connection.data.clientPubKey,
                        action = action,
                    ),
                )
            }
        }

    private suspend fun allowEvent(eventId: String) =
        withContext(dispatchers.io()) {
            database.remoteAppSessionEvents().updateSessionEventRequestState(
                eventId = eventId,
                requestState = AppRequestState.PendingResponse,
                responsePayload = null,
                completedAt = null,
            )
        }

    private suspend fun rejectEvent(eventId: String) =
        withContext(dispatchers.io()) {
            val clientPubKey = database.remoteAppSessionEvents().getSessionEvent(eventId = eventId)?.clientPubKey
            if (clientPubKey != null) {
                database.remoteAppSessionEvents().updateSessionEventRequestState(
                    eventId = eventId,
                    requestState = AppRequestState.PendingResponse,
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
