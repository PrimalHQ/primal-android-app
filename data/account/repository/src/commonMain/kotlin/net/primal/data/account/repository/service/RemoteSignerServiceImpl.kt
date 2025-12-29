package net.primal.data.account.repository.service

import io.github.aakira.napier.Napier
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.primal.core.utils.batchOnInactivity
import net.primal.core.utils.fold
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.remote.signer.mappers.mapAsRemoteSignerMethodException
import net.primal.data.account.remote.signer.mappers.mapAsRemoteSignerMethodResponse
import net.primal.data.account.remote.signer.model.RemoteSignerMethod
import net.primal.data.account.remote.signer.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.signer.parser.RemoteSignerMethodParser
import net.primal.data.account.repository.builder.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.manager.RemoteAppConnectionManager
import net.primal.data.account.repository.manager.model.RelayEvent
import net.primal.data.account.repository.mappers.getRequestType
import net.primal.data.account.repository.repository.internal.InternalSessionEventRepository
import net.primal.data.account.repository.repository.internal.model.UpdateAppSessionEventRequest
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrKeyPair

@OptIn(ExperimentalAtomicApi::class)
class RemoteSignerServiceImpl internal constructor(
    private val signerKeyPair: NostrKeyPair,
    private val sessionInactivityTimeoutInMinutes: Long,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val nostrRelayManager: NostrRelayManager,
    private val remoteSignerMethodResponseBuilder: RemoteSignerMethodResponseBuilder,
    private val internalSessionEventRepository: InternalSessionEventRepository,
    private val remoteSignerMethodParser: RemoteSignerMethodParser,
    private val remoteAppConnectionManager: RemoteAppConnectionManager,
) : RemoteSignerService {
    private val scope = CoroutineScope(SupervisorJob())

    private val relaySessionMap = AtomicReference<Map<String, List<String>>>(emptyMap())
    private val sessionActivityMap = AtomicReference<MutableMap<String, Instant>>(mutableMapOf())
    private val retrySendMethodResponseQueue = MutableSharedFlow<RemoteSignerMethodResponse>()

    override fun initialize() {
        Napier.d(tag = "Signer") { "RemoteSignerService started." }
        observeOngoingSessions()
        observePendingResponseEvents()
        observePendingNostrEvents()
        observeRelayEvents()
        observeMethods()
        observeErrors()
        observeRetryMethodResponseQueue()
        if (sessionInactivityTimeoutInMinutes > 0) {
            startInactivityLoop()
        }
    }

    private fun startInactivityLoop() =
        scope.launch {
            while (isActive) {
                delay(45.seconds)

                val now = Clock.System.now()

                val inactiveSessions = sessionActivityMap.load().filter { (_, lastActiveAt) ->
                    (lastActiveAt + sessionInactivityTimeoutInMinutes.minutes) < now
                }

                sessionRepository.endSessions(sessionIds = inactiveSessions.keys.toList())
            }
        }

    private fun observeOngoingSessions() =
        scope.launch {
            sessionRepository.observeOngoingSessions(signerPubKey = signerKeyPair.pubKey)
                .collect { sessions ->
                    relaySessionMap.store(
                        sessions
                            .flatMap { session ->
                                session.relays.map { relay -> relay to session.sessionId }
                            }
                            .groupBy(
                                keySelector = { it.first },
                                valueTransform = { it.second },
                            ),
                    )

                    sessions.forEach {
                        sessionActivityMap.load().getOrPut(it.sessionId) { Clock.System.now() }
                    }

                    nostrRelayManager.connectToRelays(relays = sessions.flatMap { it.relays }.toSet())
                }
        }

    private fun observePendingNostrEvents() =
        scope.launch {
            internalSessionEventRepository.observeRemoteAppPendingNostrEvents(signerPubKey = signerKeyPair.pubKey)
                .collect { nostrEvents ->
                    if (nostrEvents.isEmpty()) return@collect

                    nostrEvents.forEach { event ->
                        remoteSignerMethodParser.parseNostrEvent(
                            event = event,
                            signerKeyPair = signerKeyPair,
                        ).fold(
                            onSuccess = { processMethod(method = it) },
                            onFailure = { error ->
                                sendResponseOrAddToFailedQueue(
                                    response = error
                                        .mapAsRemoteSignerMethodException(nostrEvent = event)
                                        .mapAsRemoteSignerMethodResponse(),
                                )
                            },
                        )
                    }

                    internalSessionEventRepository.deleteRemoteAppPendingNostrEvents(
                        eventIds = nostrEvents.map { it.id },
                    )
                }
        }

    private fun observePendingResponseEvents() =
        scope.launch {
            internalSessionEventRepository.observeRemoteAppPendingResponseEvents(signerPubKey = signerKeyPair.pubKey)
                .collect { events ->
                    val alreadyResponded = events.mapNotNull { sessionEvent ->
                        sessionEvent.responsePayload
                            ?.decrypted
                            ?.decodeFromJsonStringOrNull<RemoteSignerMethodResponse>()
                            ?.assignClientPubKey(clientPubKey = sessionEvent.clientPubKey)
                    }

                    val toRespond = events.filter { it.responsePayload == null }
                        .mapNotNull { it.requestPayload?.decrypted?.decodeFromJsonStringOrNull<RemoteSignerMethod>() }
                        .map { remoteSignerMethodResponseBuilder.build(method = it) }

                    (alreadyResponded + toRespond)
                        .onEach { sendResponseOrAddToFailedQueue(it) }
                        .also { responses ->
                            internalSessionEventRepository.updateRemoteAppSessionEventState(
                                requests = responses.map { response ->
                                    UpdateAppSessionEventRequest(
                                        eventId = response.id,
                                        responsePayload = response.encodeToJsonString(),
                                        requestState = when (response) {
                                            is RemoteSignerMethodResponse.Error -> AppRequestState.Rejected
                                            is RemoteSignerMethodResponse.Success -> AppRequestState.Approved
                                        },
                                        completedAt = Clock.System.now().epochSeconds,
                                    )
                                },
                            )
                        }
                }
        }

    private fun observeRelayEvents() =
        scope.launch {
            nostrRelayManager.relayEvents.collect { event ->
                when (event) {
                    is RelayEvent.Connected -> {
                        relaySessionMap.load()[event.relayUrl]?.let { sessionIds ->
                            remoteAppConnectionManager.onRelayConnected(
                                sessionIds = sessionIds,
                                relayUrl = event.relayUrl,
                            )
                        }
                    }

                    is RelayEvent.Disconnected -> {
                        relaySessionMap.load()[event.relayUrl]?.let { sessionIds ->
                            remoteAppConnectionManager.onRelayDisconnected(
                                sessionIds = sessionIds,
                                relayUrl = event.relayUrl,
                                error = null,
                            )
                        }
                    }
                }
            }
        }

    private fun observeMethods() =
        scope.launch {
            nostrRelayManager.incomingMethods.collect { method ->
                Napier.d(tag = "Signer") { "Observing methods: $method" }
                processMethod(method = method)
            }
        }

    private fun observeErrors() =
        scope.launch {
            nostrRelayManager.errors.collect { error ->
                val response = error.mapAsRemoteSignerMethodResponse()
                sendResponseOrAddToFailedQueue(response = response)
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeRetryMethodResponseQueue() =
        scope.launch {
            retrySendMethodResponseQueue
                .batchOnInactivity(inactivityTimeout = 3.seconds)
                .collect { batchedResponses ->
                    batchedResponses.forEach {
                        sendResponse(it)
                    }
                }
        }

    private fun processMethod(method: RemoteSignerMethod) =
        scope.launch {
            val sessionId = sessionRepository.findFirstOpenSessionByAppIdentifier(
                appIdentifier = method.clientPubKey,
            ).getOrNull()?.sessionId
                ?: autoStartSessionIfAllowed(method.clientPubKey)
                ?: return@launch

            val canProcessMethod = connectionRepository.canProcessMethod(
                permissionId = method.getPermissionId(),
                clientPubKey = method.clientPubKey,
            )

            val response = if (canProcessMethod) {
                remoteSignerMethodResponseBuilder.build(method = method)
            } else {
                null
            }

            sessionActivityMap.load()[sessionId] = Clock.System.now()

            internalSessionEventRepository.saveRemoteAppSessionEvent(
                sessionId = sessionId,
                requestType = method.getRequestType(),
                method = method,
                signerPubKey = signerKeyPair.pubKey,
                response = response,
            )

            Napier.d(tag = "Signer") { "Response $response" }

            if (response != null) {
                sendResponseOrAddToFailedQueue(response = response)
            }
        }

    private suspend fun autoStartSessionIfAllowed(clientPubKey: String): String? {
        val connection = connectionRepository.getConnectionByClientPubKey(clientPubKey = clientPubKey).getOrNull()
        return if (connection?.autoStart == true) {
            sessionRepository.startRemoteSession(appIdentifier = connection.clientPubKey).fold(
                onSuccess = { sessionId ->
                    Napier.d(tag = "Signer") { "Session $sessionId auto-started." }
                    sessionId
                },
                onFailure = {
                    Napier.d(throwable = it, tag = "Signer") { "Failed to auto start session for $clientPubKey" }
                    val sessionId = sessionRepository.findFirstOpenSessionByAppIdentifier(appIdentifier = clientPubKey)
                        .getOrNull()?.sessionId

                    Napier.d(tag = "Signer") { "Checking existing open session for $clientPubKey: $sessionId" }

                    sessionId
                },
            )
        } else {
            Napier.d(tag = "Signer") { "Auto start sessions disabled for $clientPubKey" }
            null
        }
    }

    private suspend fun sendResponseOrAddToFailedQueue(response: RemoteSignerMethodResponse): Result<Unit> {
        return sendResponse(response).onFailure {
            Napier.d(tag = "Signer") { "Adding response to retry queue: $response" }
            retrySendMethodResponseQueue.emit(response)
        }
    }

    private suspend fun sendResponse(response: RemoteSignerMethodResponse): Result<Unit> {
        Napier.d(tag = "Signer") { "Sending response: $response" }
        val relays = connectionRepository
            .getConnectionByClientPubKey(clientPubKey = response.clientPubKey)
            .getOrNull()?.relays ?: emptyList()

        Napier.d(tag = "Signer") { "Relays: $relays" }
        return nostrRelayManager.sendResponse(
            relays = relays,
            response = response,
        ).onFailure {
            Napier.d(tag = "Signer") { "Something went wrong while sending response: ${it.message}.\n" }
        }
    }

    override fun destroy() {
        Napier.d(tag = "Signer") { "RemoteSignerService stopped." }
        scope.launch {
            sessionRepository.endAllActiveSessions()
            nostrRelayManager.disconnectFromAll()
        }.invokeOnCompletion { scope.cancel() }
    }
}
