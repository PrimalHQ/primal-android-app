package net.primal.android.nostr

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.primal.model.NostrPrimalEvent
import net.primal.android.nostr.primal.processor.NostrPrimalEventProcessor
import net.primal.android.nostr.primal.processor.PrimalEventStatsProcessor
import net.primal.android.nostr.primal.processor.PrimalReferencedEventProcessor
import net.primal.android.nostr.processor.MetadataEventProcessor
import net.primal.android.nostr.processor.NostrEventProcessor
import net.primal.android.nostr.processor.RepostEventProcessor
import net.primal.android.nostr.processor.ShortTextNoteEventProcessor
import timber.log.Timber
import javax.inject.Inject

class NostrEventsHandler @Inject constructor(
    private val database: PrimalDatabase,
) {

    private val nostrCache = hashMapOf<Int, MutableList<NostrEvent>>()
    private val nostrPrimalCache = hashMapOf<Int, MutableList<NostrPrimalEvent>>()

    fun cacheEvent(event: NostrEvent) {
        nostrCache.putIfAbsent(event.kind, mutableListOf())
        nostrCache[event.kind]?.add(event)
    }

    fun cachePrimalEvent(event: NostrPrimalEvent) {
        nostrPrimalCache.putIfAbsent(event.kind, mutableListOf())
        nostrPrimalCache[event.kind]?.add(event)
    }

    fun processCachedEvents() {
        nostrPrimalCache.keys.forEach {
            val events = nostrPrimalCache.getValue(it)
            val nostrEventKind = NostrEventKind.valueOf(it)

            Timber.d("$nostrEventKind has ${events.size} primal events.")
            Timber.i(events.toString())

            buildNostrPrimalEventProcessor(kind = nostrEventKind).process(events = events)
        }
        nostrPrimalCache.clear()

        nostrCache.keys.forEach {
            val events = nostrCache.getValue(it)
            val nostrEventKind = NostrEventKind.valueOf(it)

            Timber.d("$nostrEventKind has ${events.size} nostr events.")
            Timber.i(events.toString())

            buildNostrEventProcessor(kind = nostrEventKind).process(events = events)
        }
        nostrCache.clear()
    }

    private fun buildNostrEventProcessor(kind: NostrEventKind): NostrEventProcessor = when (kind) {
        NostrEventKind.Metadata -> MetadataEventProcessor(database = database)
        NostrEventKind.ShortTextNote -> ShortTextNoteEventProcessor(database = database)
        NostrEventKind.Reposts -> RepostEventProcessor(database = database)
        else -> throw NotImplementedError("$kind not supported.")
    }

    private fun buildNostrPrimalEventProcessor(kind: NostrEventKind): NostrPrimalEventProcessor =
        when (kind) {
            NostrEventKind.PrimalEventStats -> PrimalEventStatsProcessor(database = database)
            NostrEventKind.PrimalReferencedEvent -> PrimalReferencedEventProcessor(database = database)
            else -> throw NotImplementedError("$kind not supported.")
        }


}
