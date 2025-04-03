package net.primal.android.settings.notifications

import net.primal.domain.NotificationType

data class NotificationSwitchUi(
    val notificationType: NotificationType,
    val enabled: Boolean,
)
