package net.primal.android.settings.notifications

import net.primal.domain.notifications.NotificationType

data class NotificationSwitchUi(
    val notificationType: NotificationType,
    val enabled: Boolean,
)
