package net.primal.android.nostr.primal

import kotlinx.serialization.json.JsonObject
import net.primal.android.networking.sockets.model.isNotUnknown
import net.primal.android.networking.sockets.model.isPrimalEventKind
import net.primal.android.nostr.ext.asNostrEventOrNull
import net.primal.android.nostr.ext.asNostrPrimalEventOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.NostrPrimalEvent

class NostrEventsCache {

    val nostrCache = hashMapOf<NostrEventKind, MutableList<NostrEvent>>()
    val nostrPrimalCache = hashMapOf<NostrEventKind, MutableList<NostrPrimalEvent>>()

    fun cacheNostrEvent(kind: NostrEventKind, data: JsonObject?) {
        when {
            kind.isPrimalEventKind() -> {
                data.asNostrPrimalEventOrNull()?.let {
                    cacheNostrPrimalEvent(event = it)
                }
            }

            kind.isNotUnknown() -> {
                data.asNostrEventOrNull()?.let {
                    cacheNostrEvent(event = it)
                }
            }
        }
    }

    private fun cacheNostrEvent(event: NostrEvent) {
        val kind = NostrEventKind.valueOf(event.kind)
        nostrCache.putIfAbsent(kind, mutableListOf())
        nostrCache[kind]?.add(event)
    }

    private fun cacheNostrPrimalEvent(event: NostrPrimalEvent) {
        val kind = NostrEventKind.valueOf(event.kind)
        nostrPrimalCache.putIfAbsent(kind, mutableListOf())
        nostrPrimalCache[kind]?.add(event)
    }
}
