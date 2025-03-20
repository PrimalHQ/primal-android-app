package net.primal.core.networking.primal

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.serialization.json.JsonObject
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientImpl
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.coroutines.DispatcherProvider

internal class BasePrimalApiClient(
    dispatcherProvider: DispatcherProvider,
    private val socketClient: NostrSocketClientImpl,
) {

    private val scope = CoroutineScope(dispatcherProvider.io())

    private suspend fun <T> retrySendMessage(times: Int, block: suspend (Int) -> T): T {
        repeat(times) {
            try {
                return block(it)
            } catch (error: SocketSendMessageException) {
                Napier.w(error) { "PrimalApiClient.retry()" }
                delay(RETRY_DELAY_MILLIS)
            }
        }
        return block(times)
    }

    // TODO Revisit query solution; We should get rid of the `scope` in this class

    @OptIn(ExperimentalUuidApi::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        val queryResult = runCatching {
            retrySendMessage(MAX_RETRIES) {
                socketClient.ensureSocketConnection()
                val subscriptionId = Uuid.random().toPrimalSubscriptionId()
                val deferredQueryResult = asyncQueryCollection(subscriptionId)

                try {
                    sendMessageOrThrow(subscriptionId = subscriptionId, data = message.toPrimalJsonObject())
                } catch (error: SocketSendMessageException) {
                    deferredQueryResult.cancel(CancellationException("Unable to send socket message."))
                    throw error
                }

                deferredQueryResult.await()
            }
        }
        val result = queryResult.getOrNull()
        val error = queryResult.exceptionOrNull().takeAsWssException()
        return result ?: throw error
    }

    private fun asyncQueryCollection(subscriptionId: String): Deferred<PrimalQueryResult> {
        // TODO Revisit try/catch; this is prevents proper coroutine cancellation
        return scope.async(SupervisorJob()) {
            try {
                collectQueryResult(subscriptionId)
            } catch (error: CancellationException) {
                throw WssException(message = "Api query timed out.", cause = error)
            }
        }
    }

    private suspend fun sendMessageOrThrow(subscriptionId: String, data: JsonObject) {
        try {
            socketClient.sendREQ(subscriptionId = subscriptionId, data = data)
        } catch (error: Exception) {
            throw SocketSendMessageException(message = error.message)
        }
    }

    private fun Throwable?.takeAsWssException(): WssException {
        return when (this) {
            is WssException -> this
            is NostrNoticeException -> WssException(message = this.reason, cause = this)
            is SocketSendMessageException -> WssException(message = "Api unreachable at the moment.", cause = this)
            else -> WssException(message = this?.message, cause = this)
        }
    }

    suspend fun subscribe(subscriptionId: String, message: PrimalCacheFilter): Flow<NostrIncomingMessage> {
        socketClient.ensureSocketConnection()
        try {
            retrySendMessage(MAX_RETRIES) {
                sendMessageOrThrow(subscriptionId = subscriptionId, data = message.toPrimalJsonObject())
            }
        } catch (error: SocketSendMessageException) {
            Napier.w(error) { "Unable to subscribe." }
            throw WssException(message = "Api unreachable at the moment.", cause = error)
        }
        return socketClient.incomingMessages.filterBySubscriptionId(id = subscriptionId)
    }

    suspend fun closeSubscription(subscriptionId: String): Boolean {
        socketClient.ensureSocketConnection()
        return try {
            socketClient.sendCLOSE(subscriptionId = subscriptionId)
            true
        } catch (error: Exception) {
            false
        }
    }

    @OptIn(FlowPreview::class)
    @Throws(NostrNoticeException::class, kotlin.coroutines.cancellation.CancellationException::class)
    private suspend fun collectQueryResult(subscriptionId: String): PrimalQueryResult {
        val messages = socketClient.incomingMessages
            .filterBySubscriptionId(id = subscriptionId)
            .transformWhileEventsAreIncoming()
            .timeout(15.seconds)
            .toList()

        val terminationMessage = messages.last()

        if (terminationMessage is NostrIncomingMessage.NoticeMessage) {
            throw NostrNoticeException(
                reason = terminationMessage.message,
                subscriptionId = subscriptionId,
            )
        }

        val eventMessages = messages.filterIsInstance<NostrIncomingMessage.EventMessage>()
        val eventsMessage = messages.filterIsInstance<NostrIncomingMessage.EventsMessage>()

        val allNostrEvents = eventMessages.mapNotNull { it.nostrEvent } +
            eventsMessage.map { it.nostrEvents }.flatten()

        val allPrimalEvents = eventMessages.mapNotNull { it.primalEvent } +
            eventsMessage.map { it.primalEvents }.flatten()

        return PrimalQueryResult(
            terminationMessage = terminationMessage,
            nostrEvents = allNostrEvents,
            primalEvents = allPrimalEvents,
        )
    }

    private fun Flow<NostrIncomingMessage>.transformWhileEventsAreIncoming() =
        transformWhile {
            emit(it)
            it is NostrIncomingMessage.EventMessage || it is NostrIncomingMessage.EventsMessage
        }

    private class SocketSendMessageException(override val message: String?) : RuntimeException()

    companion object {
        const val MAX_RETRIES = 2
        private const val RETRY_DELAY_MILLIS = 500L
    }
}
