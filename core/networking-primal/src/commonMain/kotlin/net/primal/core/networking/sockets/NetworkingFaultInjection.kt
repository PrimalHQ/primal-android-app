package net.primal.core.networking.sockets

import kotlin.concurrent.Volatile
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Debug-only fault injection for reproducing the caching-socket stall in a running app, so
 * the liveness watchdog and recovery behaviour can be observed and compared across builds.
 * Everything here defaults to off and must be flipped explicitly (Settings → Developer
 * Tools). Not intended for production use.
 */
object NetworkingFaultInjection {

    /**
     * When `true`, the first "Latest" home feed request poisons the socket connection it is
     * sent on: every inbound frame on that connection is then dropped, so the Latest feed
     * never gets a response and every other request multiplexed on the same connection also
     * stalls — reproducing the reported "one unanswered request poisons the whole connection"
     * bug. A reconnect runs on a fresh connection and escapes the poison, so a build with the
     * liveness watchdog recovers on its own while a build without it stays stuck until restart.
     */
    @Volatile
    var poisonConnectionOnLatestFeed: Boolean = false

    /**
     * True when [data] is the outgoing REQ for the user's "Latest" home feed
     * (`multi_kind_mega_feed_directive` whose `spec` identifies the latest notes feed).
     */
    fun isLatestFeedRequest(data: JsonObject): Boolean {
        val cache = data[CACHE_KEY] as? JsonArray
        val verb = (cache?.getOrNull(0) as? JsonPrimitive)?.contentOrNull
        val spec = ((cache?.getOrNull(1) as? JsonObject)?.get(SPEC_KEY) as? JsonPrimitive)?.contentOrNull
        return verb == LATEST_FEED_VERB && spec?.contains(LATEST_FEED_SPEC_MARKER) == true
    }

    private const val CACHE_KEY = "cache"
    private const val SPEC_KEY = "spec"
    private const val LATEST_FEED_VERB = "multi_kind_mega_feed_directive"
    private const val LATEST_FEED_SPEC_MARKER = "\"id\":\"latest\""
}
