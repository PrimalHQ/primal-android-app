package net.primal.android.nostr.ext

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.notifications.domain.NotificationsSummary
import net.primal.android.serialization.NostrJson

fun PrimalEvent.asNotificationSummary(): NotificationsSummary {
    val summaryJsonObject = NostrJson.parseToJsonElement(this.content).jsonObject
    val pairs = NotificationType.values().mapNotNull {
        val count = summaryJsonObject[it.type.toString()]?.jsonPrimitive?.contentOrNull?.toInt()
        if (count != null) it to count else null
    }
    return NotificationsSummary(
        count = pairs.sumOf { it.second },
        countPerType = pairs.toMap(),
    )
}
