package net.primal.android.networking.relays

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.transform
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

class RelayPool @AssistedInject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    @Assisted val relays: List<Relay>,
    private val okHttpClient: OkHttpClient,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private var socketClients = listOf<NostrSocketClient>()

    init {
        initPool()
    }

    private fun List<Relay>.mapAsNostrSocketClient() =
        this
            .mapNotNull { it.toWssRequestOrNull() }
            .map {
                NostrSocketClient(
                    dispatcherProvider = dispatchers,
                    okHttpClient = okHttpClient,
                    wssRequest = it,
                )
            }

    private fun Relay.toWssRequestOrNull() =
        try {
            Request.Builder()
                .url(url)
                .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
                .build()
        } catch (error: IllegalArgumentException) {
            null
        }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        handlePublishEvent(socketClients, nostrEvent)
    }

    fun initPool() {
        socketClients = relays.mapAsNostrSocketClient()
    }

    fun closePool() {
        socketClients.forEach { it.close() }
        socketClients = emptyList()
    }

    private fun NostrPublishResult.isSuccessful(): Boolean {
        return result is NostrIncomingMessage.OkMessage && result.success
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
                        responseFlow.emit(NostrPublishResult(error = error))
                    } catch (error: TimeoutCancellationException) {
                        responseFlow.emit(NostrPublishResult(error = error))
                    }
                }
            }
        }

        responseFlow.timeout(30.seconds)
            .catch { throw NostrPublishException(cause = it) }
            .first { it.isSuccessful() }
    }
}
