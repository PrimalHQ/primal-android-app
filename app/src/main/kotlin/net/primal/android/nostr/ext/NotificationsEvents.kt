package net.primal.android.nostr.ext

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.notifications.domain.NotificationsSummary
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.common.PrimalEvent
import net.primal.domain.notifications.NotificationType

fun PrimalEvent.asNotificationSummary(): NotificationsSummary {
    val summaryJsonObject = CommonJson.parseToJsonElement(this.content).jsonObject
    val pairs = NotificationType.entries.mapNotNull {
        val count = summaryJsonObject[it.type.toString()]?.jsonPrimitive?.contentOrNull?.toInt()
        if (count != null) it to count else null
    }
    return NotificationsSummary(
        count = pairs.sumOf { it.second },
        countPerType = pairs.toMap(),
    )
}
