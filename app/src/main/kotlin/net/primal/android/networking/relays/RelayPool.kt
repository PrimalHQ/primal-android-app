package net.primal.android.networking.relays

import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.filterByEventId
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.user.domain.Relay
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class RelayPool @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
) {

    private val scope = CoroutineScope(dispatchers.io())

    var relays: List<Relay> = emptyList()
        private set

    private var socketClients = listOf<NostrSocketClient>()

    private val _relayPoolStatus = MutableStateFlow(mapOf<String, Boolean>())
    val relayPoolStatus = _relayPoolStatus.asStateFlow()
    private fun updateRelayStatus(url: String, connected: Boolean) {
        scope.launch {
            _relayPoolStatus.getAndUpdate {
                it.toMutableMap().apply { this[url] = connected }
            }
        }
    }

    private fun updatePoolStatus(relays: List<Relay>) {
        scope.launch {
            _relayPoolStatus.update {
                mutableMapOf<String, Boolean>().apply {
                    relays.forEach { this[it.url] = false }
                }
            }
        }
    }

    fun changeRelays(relays: List<Relay>) {
        closePool()
        socketClients = relays.mapAsNostrSocketClient()
        this.relays = relays
        updatePoolStatus(relays)
    }

    fun closePool() {
        socketClients.forEach { it.close() }
        socketClients = emptyList()
        updatePoolStatus(emptyList())
    }

    fun hasRelays() = relays.isNotEmpty()

    private fun List<Relay>.mapAsNostrSocketClient() =
        this
            .mapNotNull { it.toWssRequestOrNull() }
            .map {
                NostrSocketClient(
                    dispatcherProvider = dispatchers,
                    okHttpClient = okHttpClient,
                    wssRequest = it,
                    onSocketConnectionOpened = { url -> updateRelayStatus(url = url, connected = true) },
                    onSocketConnectionClosed = { url, _ -> updateRelayStatus(url = url, connected = false) },
                )
            }

    private fun Relay.toWssRequestOrNull() =
        try {
            Request.Builder()
                .url(url)
                .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
                .build()
        } catch (error: IllegalArgumentException) {
            Timber.w(error)
            null
        }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        handlePublishEvent(socketClients, nostrEvent)
    }

    @OptIn(FlowPreview::class)
    private suspend fun handlePublishEvent(relayConnections: List<NostrSocketClient>, nostrEvent: NostrEvent) {
        val responseFlow = MutableSharedFlow<NostrPublishResult>()
        relayConnections.forEach { nostrSocketClient ->
            scope.launch {
                with(nostrSocketClient) {
                    ensureSocketConnection()
                    sendEVENT(nostrEvent.toJsonObject())
                    try {
                        val response = collectPublishResponse(eventId = nostrEvent.id)
                        responseFlow.emit(NostrPublishResult(result = response))
                    } catch (error: NostrNoticeException) {
                        Timber.w(error)
                        responseFlow.emit(NostrPublishResult(error = error))
                    } catch (error: TimeoutCancellationException) {
                        Timber.w(error)
                        responseFlow.emit(NostrPublishResult(error = error))
                    }
                }
            }
        }

        responseFlow.timeout(30.seconds)
            .catch { throw NostrPublishException(cause = it) }
            .first { it.isSuccessful() }
    }

    @FlowPreview
    private suspend fun NostrSocketClient.collectPublishResponse(eventId: String): NostrIncomingMessage.OkMessage {
        return incomingMessages
            .filterByEventId(id = eventId)
            .transform {
                when (it) {
                    is NostrIncomingMessage.OkMessage -> emit(it)
                    is NostrIncomingMessage.NoticeMessage -> throw NostrNoticeException(
                        reason = it.message,
                    )

                    else -> throw IllegalStateException("$it is not allowed")
                }
            }
            .timeout(30.seconds)
            .first()
    }

    private fun NostrPublishResult.isSuccessful(): Boolean {
        return result is NostrIncomingMessage.OkMessage && result.success
    }
}
