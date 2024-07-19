package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalWordCount

fun List<PrimalEvent>.flatMapAsWordCount(): Map<String, Int> {
    return this.mapNotNull {
        NostrJson.decodeFromStringOrNull<ContentPrimalWordCount>(it.content)
    }.associate {
        it.eventId to it.words
    }
}
