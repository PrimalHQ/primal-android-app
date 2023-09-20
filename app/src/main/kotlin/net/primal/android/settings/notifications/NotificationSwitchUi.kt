package net.primal.android.settings.notifications

import net.primal.android.notifications.domain.NotificationType

data class NotificationSwitchUi(
    val notificationType: NotificationType,
    val enabled: Boolean,
)
