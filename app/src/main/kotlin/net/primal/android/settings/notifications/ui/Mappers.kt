package net.primal.android.settings.notifications.ui

import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.notifications.NotificationSettingsType.Preferences
import net.primal.domain.notifications.NotificationSettingsType.PushNotifications
import net.primal.domain.notifications.NotificationSettingsType.TabNotifications
import net.primal.domain.notifications.NotificationType

fun ContentAppSettings.mapAsPushNotificationSwitchUi() =
    pushNotifications.toMap().let { remoteMap ->
        listOf(
            PushNotifications.NewFollows,
            PushNotifications.Zaps,
            PushNotifications.Reactions,
            PushNotifications.Replies,
            PushNotifications.Reposts,
            PushNotifications.Mentions,
            PushNotifications.DirectMessages,
            PushNotifications.WalletTransactions,
            PushNotifications.LiveEvents,
        ).map { pushNotificationType ->
            val enabled = remoteMap[pushNotificationType.id]?.jsonPrimitive?.booleanOrNull ?: false
            NotificationSwitchUi<PushNotifications>(settingsType = pushNotificationType, enabled = enabled)
        }
    }
        .sortedBy { it.settingsType.order }

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
                TabNotifications.LiveEvents.resolveIfEnabled(remoteMap),
            )
        }.map {
            NotificationSwitchUi<TabNotifications>(settingsType = it.first, enabled = it.second)
        }

fun ContentAppSettings.mapAsNotificationsPreferences() =
    notificationsAdditional.toMap().let { remoteMap ->
        listOf(
            Preferences.ReplyRoReply,
            Preferences.HellThread,
            Preferences.DMsFromFollows,
            Preferences.ReactionsFromFollows,
        ).map { preferenceType ->
            val enabled = remoteMap[preferenceType.id]?.jsonPrimitive?.booleanOrNull ?: false
            NotificationSwitchUi(settingsType = preferenceType, enabled = enabled)
        }
    }
