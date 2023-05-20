package net.primal.android.nostr.primal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.NostrEventsHandler
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.NostrKindEventRange
import net.primal.android.nostr.model.NostrVerb
import net.primal.android.nostr.primal.model.request.FeedRequest
import net.primal.android.nostr.primal.model.request.SearchContentRequest
import net.primal.android.serialization.NostrJson
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class PrimalApiImpl @Inject constructor(
    private val socketClient: SocketClient,
    private val nostrEventsHandler: NostrEventsHandler,
) : PrimalApi {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun requestFeedUpdates(request: FeedRequest) = launchSubscription {
        socketClient.sendRequest(
            request = OutgoingMessage(
                command = "feed",
                options = NostrJson.encodeToString(
                    FeedRequest(
                        pubKey = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2",
                        userPubKey = "460c25e682fda7832b52d1f22d3d22b3176d972f60dcdc3212ed8c92ef85065c",
                    )
                )
            )
        )
    }

    override fun searchContent(request: SearchContentRequest) = launchSubscription {
        socketClient.sendRequest(
            request = OutgoingMessage(
                command = "search",
                options = NostrJson.encodeToString(
                    SearchContentRequest(
                        query = request.query,
                    )
                )
            )
        )
    }

    private fun launchSubscription(socketSubscribing: () -> UUID) {
        val subscriptionId = socketSubscribing()
        scope.launch {
            socketClient.messagesBySubscriptionId(subscriptionId).cancellable().collect {
                when (it.type) {
                    NostrVerb.Incoming.EVENT -> if (it.data != null) {
                        val kind = it.data["kind"]?.jsonPrimitive?.content?.toIntOrNull()
                        val nostrEventKind = NostrEventKind.valueOf(kind ?: -1)
                        when {
                            nostrEventKind.value in NostrKindEventRange.PrimalEvents -> {
                                nostrEventsHandler.cachePrimalEvent(
                                    event = NostrJson.decodeFromJsonElement(it.data)
                                )
                            }

                            nostrEventKind == NostrEventKind.Unknown -> {
                                Timber.w(
                                    "An unknown kind ($kind) of nostr event " +
                                            "detected in the incoming message: $it"
                                )
                            }

                            else -> {
                                nostrEventsHandler.cacheEvent(
                                    event = NostrJson.decodeFromJsonElement(it.data)
                                )
                            }
                        }
                    } else {
                        Timber.e("Unable to process incoming message: $it")
                    }

                    NostrVerb.Incoming.EOSE -> {
                        nostrEventsHandler.processCachedEvents()
                        cancel()
                    }

                    NostrVerb.Incoming.NOTICE -> {
                        Timber.e("NOTICE: $it")
                        cancel()
                    }

                    else -> Timber.e("Ignored incoming message: $it")
                }
            }
        }
    }
}
