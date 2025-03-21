package net.primal.data.remote.mapper

import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.primal.core.utils.serialization.CommonJson
import net.primal.data.remote.model.ConversationSummary
import net.primal.data.remote.model.ConversationsSummary
import net.primal.domain.PrimalEvent

fun PrimalEvent.asMessageConversationsSummary(): ConversationsSummary {
    val jsonObject = CommonJson.parseToJsonElement(this.content).jsonObject
    val map = mutableMapOf<String, ConversationSummary>()
    jsonObject.keys.forEach {
        jsonObject[it]?.jsonObject?.let { summaryJson ->
            val summary = CommonJson.decodeFromJsonElement<ConversationSummary>(summaryJson)
            map[it] = summary
        }
    }
    return ConversationsSummary(summaryPerParticipantId = map)
}
