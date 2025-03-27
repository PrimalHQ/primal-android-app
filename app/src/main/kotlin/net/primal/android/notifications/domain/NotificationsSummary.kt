package net.primal.android.notifications.domain

import net.primal.domain.NotificationType

data class NotificationsSummary(
    val count: Int,
    val countPerType: Map<NotificationType, Int> = mapOf(),
)
