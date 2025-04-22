package net.primal.core.networking.primal

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

internal class BasePrimalApiClient(
    private val socketClient: NostrSocketClientImpl,
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        return try {
            coroutineScope {
                socketClient.ensureSocketConnectionOrThrow()
                val subscriptionId = Uuid.random().toPrimalSubscriptionId()
                val deferredQueryResult = async { collectQueryResult(subscriptionId) }
                sendMessageOrThrow(subscriptionId = subscriptionId, data = message.toPrimalJsonObject())
                deferredQueryResult.await()
            }
        } catch (error: Exception) {
            throw error.takeAsWssException(verb = message.primalVerb)
        }
    }

    private suspend fun sendMessageOrThrow(subscriptionId: String, data: JsonObject) {
        try {
            socketClient.sendREQ(subscriptionId = subscriptionId, data = data)
        } catch (error: Exception) {
            throw SocketSendMessageException(message = error.message)
        }
    }

    private fun Throwable?.takeAsWssException(verb: String?): WssException {
        return when (this) {
            is WssException -> this
            is NostrNoticeException -> WssException(message = "${this.reason} [$verb]")
            is SocketSendMessageException -> WssException(message = "Api unreachable at the moment. [$verb]")
            else -> WssException(message = "${this?.message} [$verb]", cause = this)
        }
    }

    suspend fun subscribe(subscriptionId: String, message: PrimalCacheFilter): Flow<NostrIncomingMessage> {
        try {
            socketClient.ensureSocketConnectionOrThrow()
            sendMessageOrThrow(subscriptionId = subscriptionId, data = message.toPrimalJsonObject())
        } catch (error: Exception) {
            Napier.w(error) { "Unable to subscribe." }
            throw WssException(message = "Api unreachable at the moment.", cause = error)
        }
        return socketClient.incomingMessages.filterBySubscriptionId(id = subscriptionId)
    }

    suspend fun closeSubscription(subscriptionId: String): Boolean {
        return try {
            socketClient.ensureSocketConnectionOrThrow()
            socketClient.sendCLOSE(subscriptionId = subscriptionId)
            true
        } catch (_: Exception) {
            false
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun collectQueryResult(subscriptionId: String): PrimalQueryResult {
        val messages = socketClient.incomingMessages
            .filterBySubscriptionId(id = subscriptionId)
            .transformWhileEventsAreIncoming()
            .timeout(15.seconds)
            .toList()

        val terminationMessage = messages.lastOrNull()
        terminationMessage.verifyOrThrow(subscriptionId)
        checkNotNull(terminationMessage)

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

    private fun NostrIncomingMessage?.verifyOrThrow(subscriptionId: String) {
        if (this == null) {
            throw WssException("No messages received.")
        }

        if (this is NostrIncomingMessage.NoticeMessage) {
            throw NostrNoticeException(
                reason = this.message,
                subscriptionId = subscriptionId,
            )
        }
    }

    private fun Flow<NostrIncomingMessage>.transformWhileEventsAreIncoming() =
        transformWhile {
            emit(it)
            it is NostrIncomingMessage.EventMessage || it is NostrIncomingMessage.EventsMessage
        }

    private class SocketSendMessageException(override val message: String?) : RuntimeException()
}
