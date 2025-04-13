package net.primal.android.notifications.domain

import net.primal.domain.notifications.NotificationType

data class NotificationsSummary(
    val count: Int,
    val countPerType: Map<NotificationType, Int> = mapOf(),
)
