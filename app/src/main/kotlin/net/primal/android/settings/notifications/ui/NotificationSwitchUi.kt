package net.primal.android.settings.notifications.ui

import net.primal.domain.notifications.NotificationSettingsType

data class NotificationSwitchUi<T : NotificationSettingsType>(
    val settingsType: T,
    val enabled: Boolean,
)
