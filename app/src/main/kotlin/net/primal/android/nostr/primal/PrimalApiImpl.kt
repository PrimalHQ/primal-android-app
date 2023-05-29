package net.primal.android.nostr.primal

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.FeedPostDataCrossRef
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.networking.sockets.model.getMessageNostrEventKind
import net.primal.android.nostr.ext.mapNotNullAsPost
import net.primal.android.nostr.ext.mapNotNullAsRepost
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.NostrVerb
import net.primal.android.nostr.model.primal.NostrPrimalEvent
import net.primal.android.nostr.model.primal.request.FeedRequest
import net.primal.android.nostr.model.primal.request.SearchContentRequest
import net.primal.android.nostr.processor.NostrEventProcessorFactory
import net.primal.android.nostr.processor.primal.NostrPrimalEventProcessorFactory
import net.primal.android.serialization.NostrJson
import timber.log.Timber
import javax.inject.Inject

class PrimalApiImpl @Inject constructor(
    private val socketClient: SocketClient,
    private val database: PrimalDatabase,
) : PrimalApi {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun requestDefaultAppSettings() = launchQuery(
        outgoingMessage = OutgoingMessage(primalVerb = "get_default_app_settings", options = null)
    )

    override fun searchContent(query: String) = launchQuery(
        outgoingMessage = OutgoingMessage(
            primalVerb = "search",
            options = NostrJson.encodeToString(
                SearchContentRequest(
                    query = query,
                )
            )
        )
    )

    override fun requestFeedUpdates(feedDirective: String, userPubkey: String) = launchQuery(
        outgoingMessage = OutgoingMessage(
            primalVerb = "feed_directive",
            options = NostrJson.encodeToString(
                FeedRequest(
                    directive = feedDirective,
                    userPubKey = userPubkey,
                    limit = 100,
                )
            )
        ),

        onNostrEvents = { resultMap ->
            resultMap.processShortTextNotesAndReposts(feedDirective = feedDirective)
            resultMap.filterKeys { it != NostrEventKind.Reposts && it != NostrEventKind.ShortTextNote }
                .processAllNostrEvents()
        },

        onNostrPrimalEvents = { resultMap -> resultMap.processAllNostrPrimalEvents() }
    )

    private fun <T> launchQuery(
        outgoingMessage: OutgoingMessage<T>,
        onNostrEvents: suspend (Map<NostrEventKind, List<NostrEvent>>) -> Unit,
        onNostrPrimalEvents: suspend (Map<NostrEventKind, List<NostrPrimalEvent>>) -> Unit,
        onNotice: (String?) -> Unit = {},
    ) = scope.launch {
        val cache = NostrEventsCache()
        val subscriptionId = socketClient.sendRequest(message = outgoingMessage)
        socketClient.messagesBySubscriptionId(subscriptionId).cancellable().collect { inMessage ->
            when (inMessage.type) {
                NostrVerb.Incoming.EVENT -> cache.cacheNostrEvent(
                    kind = inMessage.getMessageNostrEventKind(),
                    data = inMessage.data,
                ).also {
                    Timber.d(inMessage.toString())
                }

                NostrVerb.Incoming.EOSE -> {
                    onNostrEvents(cache.nostrCache)
                    onNostrPrimalEvents(cache.nostrPrimalCache)
                    cancel()
                }

                NostrVerb.Incoming.NOTICE -> {
                    onNotice(inMessage.data?.jsonPrimitive?.content)
                    cancel()
                }

                else -> Timber.e("Ignored incoming message: $inMessage")
            }
        }
    }

    private fun <T> launchQuery(
        outgoingMessage: OutgoingMessage<T>,
        onNotice: (String?) -> Unit = {},
    ) = scope.launch {
        val cache = NostrEventsCache()
        val subscriptionId = socketClient.sendRequest(message = outgoingMessage)
        socketClient.messagesBySubscriptionId(subscriptionId).cancellable().collect { inMessage ->
            when (inMessage.type) {
                NostrVerb.Incoming.EVENT -> cache.cacheNostrEvent(
                    kind = inMessage.getMessageNostrEventKind(),
                    data = inMessage.data,
                ).also {
                    Timber.d(inMessage.toString())
                }

                NostrVerb.Incoming.EOSE -> {
                    cache.nostrCache.processAllNostrEvents()
                    cache.nostrPrimalCache.processAllNostrPrimalEvents()
                    cancel()
                }

                NostrVerb.Incoming.NOTICE -> {
                    onNotice(inMessage.data?.jsonPrimitive?.content)
                    cancel()
                }

                else -> Timber.e("Ignored incoming message: $inMessage")
            }
        }
    }

    private suspend fun Map<NostrEventKind, List<NostrEvent>>.processShortTextNotesAndReposts(
        feedDirective: String
    ) {
        val shortTextNoteEvents = this[NostrEventKind.ShortTextNote]
        val repostEvents = this[NostrEventKind.Reposts]
        database.withTransaction {
            val posts = shortTextNoteEvents?.mapNotNullAsPost() ?: emptyList()
            val reposts = repostEvents?.mapNotNullAsRepost() ?: emptyList()
            Timber.i("Received ${posts.size} posts and ${reposts.size} reposts..")

            database.posts().upsertAll(data = posts)
            database.reposts().upsertAll(data = reposts)

            val feedConnections = posts.map { it.postId } + reposts.map { it.postId }
            database.feedsConnections().connect(
                data = feedConnections.map { postId ->
                    FeedPostDataCrossRef(
                        feedDirective = feedDirective,
                        postId = postId
                    )
                }
            )
        }
    }

    private fun Map<NostrEventKind, List<NostrEvent>>.processAllNostrEvents() {
        val factory = NostrEventProcessorFactory(database = database)
        this.keys.forEach { kind ->
            val events = getValue(kind)
            Timber.i("$kind has ${events.size} nostr events.")
            factory.create(kind)?.process(events = events)
        }
    }

    private fun Map<NostrEventKind, List<NostrPrimalEvent>>.processAllNostrPrimalEvents() {
        val factory = NostrPrimalEventProcessorFactory(database = database)
        this.keys.forEach { kind ->
            val events = getValue(kind)
            Timber.i("$kind has ${events.size} nostr primal events.")
            factory.create(kind)?.process(events = events)
        }
    }

}
