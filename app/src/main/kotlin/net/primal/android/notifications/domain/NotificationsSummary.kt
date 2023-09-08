package net.primal.android.notifications.domain

data class NotificationsSummary(
    val count: Int,
    val countPerType: Map<NotificationType, Int> = mapOf(),
)
