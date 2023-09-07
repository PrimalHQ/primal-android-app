package net.primal.android.networking.primal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.networking.sockets.filterBySubscriptionId
import java.util.UUID
import javax.inject.Inject

class PrimalClient @Inject constructor(
    private val socketClient: NostrSocketClient,
) {

    @Throws(WssException::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult {
        var queryAttempts = 0
        var subscriptionId: UUID? = null
        while (queryAttempts < MAX_QUERY_ATTEMPTS) {
            socketClient.ensureSocketConnection()
            val data = message.toPrimalJsonObject()
            subscriptionId = socketClient.sendREQ(data = data)
            queryAttempts++

            if (subscriptionId != null) break

            if (queryAttempts < MAX_QUERY_ATTEMPTS) {
                delay(RETRY_DELAY_MILLIS)
            }
        }

        return if (subscriptionId != null) {
            try {
                collectQueryResult(subscriptionId = subscriptionId)
            } catch (error: NostrNoticeException) {
                throw WssException(
                    message = error.reason,
                    cause = error,
                )
            }
        } else {
            throw WssException(message = "Api unreachable at the moment.")
        }
    }

    @Throws(NostrNoticeException::class)
    private suspend fun collectQueryResult(subscriptionId: UUID): PrimalQueryResult {
        val messages = socketClient.incomingMessages
            .transformWhileEventsAreIncoming(subscriptionId)
            .toList()

        val terminationMessage = messages.last()

        if (terminationMessage is NostrIncomingMessage.NoticeMessage) {
            throw NostrNoticeException(reason = terminationMessage.message)
        }

        val events = messages.filterIsInstance(NostrIncomingMessage.EventMessage::class.java)

        val nostrEvents = events.mapNotNull { it.nostrEvent }
        val primalEvents = events.mapNotNull { it.primalEvent }

        return PrimalQueryResult(
            terminationMessage = terminationMessage,
            nostrEvents = nostrEvents,
            primalEvents = primalEvents,
        )
    }

    private fun Flow<NostrIncomingMessage>.transformWhileEventsAreIncoming(subscriptionId: UUID) =
        this.filterBySubscriptionId(id = subscriptionId).transformWhile {
            emit(it)
            it is NostrIncomingMessage.EventMessage
        }

    companion object {
        private const val MAX_QUERY_ATTEMPTS = 3
        private const val RETRY_DELAY_MILLIS = 500L
    }

}
