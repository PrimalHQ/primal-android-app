package net.primal.android.settings.notifications.ui

import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.notifications.NotificationSettingsType.Preferences
import net.primal.domain.notifications.NotificationSettingsType.PushNotifications
import net.primal.domain.notifications.NotificationSettingsType.TabNotifications
import net.primal.domain.notifications.NotificationType

fun ContentAppSettings.mapAsPushNotificationSwitchUi() =
    pushNotifications.toMap()
        .mapNotNull {
            val type = PushNotifications.valueOf(id = it.key)
            val enabled = it.value.jsonPrimitive.booleanOrNull
            if (type != null && enabled != null) {
                NotificationSwitchUi<PushNotifications>(settingsType = type, enabled = enabled)
            } else {
                null
            }
        }

private fun TabNotifications.resolveIfEnabled(
    remoteMap: Map<NotificationType, Boolean>,
): Pair<TabNotifications, Boolean> {
    return this to remoteMap.run {
        this@resolveIfEnabled.types.any {
            remoteMap[it] == true
        }
    }
}

fun ContentAppSettings.mapAsTabNotificationSwitchUi() =
    notifications.toMap()
        .mapNotNull {
            val type = NotificationType.valueOf(id = it.key)
            val enabled = it.value.jsonPrimitive.booleanOrNull
            if (type != null && enabled != null) type to enabled else null
        }
        .associate { it.first to it.second }
        .let { remoteMap ->
            listOf(
                TabNotifications.NewFollows.resolveIfEnabled(remoteMap),
                TabNotifications.Zaps.resolveIfEnabled(remoteMap),
                TabNotifications.Reactions.resolveIfEnabled(remoteMap),
                TabNotifications.Replies.resolveIfEnabled(remoteMap),
                TabNotifications.Reposts.resolveIfEnabled(remoteMap),
                TabNotifications.Mentions.resolveIfEnabled(remoteMap),
            )
        }.map {
            NotificationSwitchUi<TabNotifications>(settingsType = it.first, enabled = it.second)
        }

fun ContentAppSettings.mapAsNotificationsPreferences() =
    notificationsAdditional.toMap()
        .mapNotNull {
            val type = Preferences.valueOf(id = it.key)
            val enabled = it.value.jsonPrimitive.booleanOrNull
            if (type != null && enabled != null) {
                NotificationSwitchUi<Preferences>(settingsType = type, enabled = enabled)
            } else {
                null
            }
        }
