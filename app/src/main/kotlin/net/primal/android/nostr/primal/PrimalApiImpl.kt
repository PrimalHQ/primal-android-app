package net.primal.android.nostr.primal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.db.PrimalDatabase
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
import javax.inject.Inject

class PrimalApiImpl @Inject constructor(
    private val socketClient: SocketClient,
    private val database: PrimalDatabase,
) : PrimalApi {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun requestDefaultAppSettings() = launchSubscription {
        OutgoingMessage(primalVerb = "get_default_app_settings", options = null)
    }

    override fun requestFeedUpdates(request: FeedRequest) = launchSubscription {
        OutgoingMessage(primalVerb = "feed", options = NostrJson.encodeToString(request))
    }

    override fun searchContent(request: SearchContentRequest) = launchSubscription {
        OutgoingMessage(
            primalVerb = "search",
            options = NostrJson.encodeToString(
                SearchContentRequest(
                    query = request.query,
                )
            )
        )
    }

    private fun <T> launchSubscription(
        messageBuilder: () -> OutgoingMessage<T>,

    ) {
        val subscriptionId = socketClient.sendRequest(message = messageBuilder())
        scope.launch {
            val nostrEventsHandler = NostrEventsHandler(database = database)
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
