package net.primal.data.account.repository.service

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.RequestState
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.processor.RemoteSignerMethodProcessor
import net.primal.data.account.repository.builder.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.manager.model.RelayEvent
import net.primal.data.account.repository.repository.InternalSessionEventRepository
import net.primal.data.account.repository.repository.model.UpdateSessionEventRequest
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerServiceImpl internal constructor(
    private val signerKeyPair: NostrKeyPair,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val nostrRelayManager: NostrRelayManager,
    private val remoteSignerMethodResponseBuilder: RemoteSignerMethodResponseBuilder,
    private val internalSessionEventRepository: InternalSessionEventRepository,
    private val methodProcessor: RemoteSignerMethodProcessor,
) : RemoteSignerService {
    private val scope = CoroutineScope(SupervisorJob())

    private var activeRelays = emptySet<String>()
    private var relaySessionMap = emptyMap<String, List<String>>()
    private var activeClientPubKeys = HashSet<String>()
    private var clientSessionMap = emptyMap<String, String>()
    private val sessionActivityMap = mutableMapOf<String, Instant>()

    companion object {
        private val SESSION_INACTIVITY_TIMEOUT = 15.minutes
    }

    override fun initialize() {
        Napier.d(tag = "Signer") { "RemoteSignerService started." }
        observeOngoingSessions()
        observePendingResponseEvents()
        observePendingNostrEvents()
        observeRelayEvents()
        observeMethods()
        observeErrors()
        startInactivityLoop()
    }

    private fun startInactivityLoop() =
        scope.launch {
            while (isActive) {
                delay(45.seconds)

                val now = Clock.System.now()

                sessionActivityMap.forEach { (sessionId, lastActiveAt) ->
                    if ((lastActiveAt + SESSION_INACTIVITY_TIMEOUT) < now) {
                        sessionRepository.endSession(sessionId = sessionId)
                    }
                }
            }
        }

    private fun observeOngoingSessions() =
        scope.launch {
            sessionRepository.observeOngoingSessions(signerPubKey = signerKeyPair.pubKey)
                .collect { sessions ->
                    relaySessionMap = sessions
                        .flatMap { session ->
                            session.relays.map { relay -> relay to session.sessionId }
                        }
                        .groupBy(
                            keySelector = { it.first },
                            valueTransform = { it.second },
                        )

                    sessions.forEach {
                        setActiveRelays(it)
                        sessionActivityMap.getOrPut(it.sessionId) { Clock.System.now() }
                    }
                    clientSessionMap = sessions.associate { it.clientPubKey to it.sessionId }
                    activeClientPubKeys = sessions.map { it.clientPubKey }.toHashSet()
                    nostrRelayManager.connectToRelays(relays = sessions.flatMap { it.relays }.toSet())
                }
        }

    private fun observePendingNostrEvents() =
        scope.launch {
            internalSessionEventRepository.observePendingNostrEvents(signerPubKey = signerKeyPair.pubKey)
                .collect { nostrEvents ->
                    if (nostrEvents.isEmpty()) return@collect

                    nostrEvents.forEach { event ->
                        methodProcessor.processNostrEvent(
                            event = event,
                            signerKeyPair = signerKeyPair,
                            onFailure = { scope.launch { sendResponse(response = it) } },
                            onSuccess = { scope.launch { processMethod(method = it) } },
                        )
                    }

                    internalSessionEventRepository.deletePendingNostrEvents(eventIds = nostrEvents.map { it.id })
                }
        }

    private fun observePendingResponseEvents() =
        scope.launch {
            internalSessionEventRepository.observePendingResponseEvents(signerPubKey = signerKeyPair.pubKey)
                .collect { events ->
                    val alreadyResponded = events.mapNotNull {
                        it.responsePayload?.decrypted?.decodeFromJsonStringOrNull<RemoteSignerMethodResponse>()
                    }

                    val toRespond = events.filter { it.responsePayload == null }
                        .mapNotNull { it.requestPayload?.decrypted?.decodeFromJsonStringOrNull<RemoteSignerMethod>() }
                        .map { remoteSignerMethodResponseBuilder.build(method = it) }

                    (alreadyResponded + toRespond)
                        .onEach { sendResponse(it) }
                        .also { responses ->
                            internalSessionEventRepository.updateSessionEventState(
                                requests = responses.map { response ->
                                    UpdateSessionEventRequest(
                                        eventId = response.id,
                                        responsePayload = response.encodeToJsonString(),
                                        requestState = when (response) {
                                            is RemoteSignerMethodResponse.Error -> RequestState.Rejected
                                            is RemoteSignerMethodResponse.Success -> RequestState.Approved
                                        },
                                        completedAt = Clock.System.now().epochSeconds,
                                    )
                                },
                            )
                        }
                }
        }

    private suspend fun setActiveRelays(session: AppSession) {
        sessionRepository.setActiveRelayCount(
            sessionId = session.sessionId,
            activeRelayCount = session.relays.map { activeRelays.contains(it) }.count { it },
        )
    }

    private fun observeRelayEvents() =
        scope.launch {
            nostrRelayManager.relayEvents.collect { event ->
                when (event) {
                    is RelayEvent.Connected -> {
                        activeRelays = activeRelays + event.relayUrl
                        relaySessionMap[event.relayUrl]?.let {
                            sessionRepository.incrementActiveRelayCount(sessionIds = it)
                        }
                    }

                    is RelayEvent.Disconnected -> {
                        activeRelays = activeRelays - event.relayUrl
                        relaySessionMap[event.relayUrl]?.let {
                            sessionRepository.decrementActiveRelayCountOrEnd(sessionIds = it)
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
                sendResponse(response = error)
            }
        }

    private fun processMethod(method: RemoteSignerMethod) =
        scope.launch {
            if (!activeClientPubKeys.contains(method.clientPubKey)) {
                val connection = connectionRepository
                    .getConnectionByClientPubKey(clientPubKey = method.clientPubKey).getOrNull() ?: return@launch

                if (connection.autoStart) {
                    sessionRepository.startSession(connectionId = connection.connectionId)
                } else {
                    return@launch
                }
            }

            val canProcessMethod = connectionRepository.canProcessMethod(
                permissionId = method.getPermissionId(),
                clientPubKey = method.clientPubKey,
            )

            val response = if (canProcessMethod) {
                remoteSignerMethodResponseBuilder.build(method = method)
            } else {
                null
            }

            findActiveSessionId(clientPubKey = method.clientPubKey)?.let { sessionId ->
                sessionActivityMap[sessionId] = Clock.System.now()

                internalSessionEventRepository.saveSessionEvent(
                    sessionId = sessionId,
                    method = method,
                    signerPubKey = signerKeyPair.pubKey,
                    response = response,
                )
            }

            Napier.d(tag = "Signer") { "Response $response" }

            if (response != null) {
                sendResponse(response = response)
            }
        }

    private suspend fun findActiveSessionId(clientPubKey: String): String? =
        clientSessionMap[clientPubKey]
            ?: connectionRepository.getConnectionByClientPubKey(clientPubKey = clientPubKey).getOrNull()
                ?.let { sessionRepository.findActiveSessionForConnection(connectionId = it.connectionId) }
                ?.getOrNull()?.sessionId

    private suspend fun sendResponse(response: RemoteSignerMethodResponse) {
        Napier.d(tag = "Signer") { "Sending response: $response" }
        val relays = connectionRepository
            .getConnectionByClientPubKey(clientPubKey = response.clientPubKey)
            .getOrNull()?.relays

        Napier.d(tag = "Signer") { "Relays: $relays" }

        nostrRelayManager.sendResponse(
            relays = relays ?: return,
            response = response,
        )
    }

    override fun destroy() {
        Napier.d(tag = "Signer") { "RemoteSignerService stopped." }
        scope.launch {
            sessionRepository.endAllActiveSessions()
            nostrRelayManager.disconnectFromAll()
        }.invokeOnCompletion { scope.cancel() }
    }
}
