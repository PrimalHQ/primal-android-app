package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.PrimalEvent

fun List<PrimalEvent>.flatMapAsWordCount(): Map<String, Int> {
    return this.mapNotNull {
        CommonJson.decodeFromStringOrNull<ContentPrimalWordCount>(it.content)
    }.associate {
        it.eventId to it.words
    }
}
