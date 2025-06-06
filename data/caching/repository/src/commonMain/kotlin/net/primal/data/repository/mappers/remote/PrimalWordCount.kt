package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.common.PrimalEvent

fun List<PrimalEvent>.flatMapAsWordCount(): Map<String, Int> {
    return this.mapNotNull {
        it.content.decodeFromJsonStringOrNull<ContentPrimalWordCount>()
    }.associate {
        it.eventId to it.words
    }
}
