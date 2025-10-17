package net.primal.android.settings.notifications.ui

import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.notifications.NotificationSettingsType.Preferences
import net.primal.domain.notifications.NotificationSettingsType.PushNotifications
import net.primal.domain.notifications.NotificationSettingsType.TabNotifications

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
            val enabled = remoteMap[pushNotificationType.id]?.jsonPrimitive?.booleanOrNull ?: true
            NotificationSwitchUi(settingsType = pushNotificationType, enabled = enabled)
        }
    }
        .sortedBy { it.settingsType.order }

fun ContentAppSettings.mapAsTabNotificationSwitchUi() =
    notifications.toMap().let { remoteMap ->
        listOf(
            TabNotifications.NewFollows,
            TabNotifications.Zaps,
            TabNotifications.Reactions,
            TabNotifications.Replies,
            TabNotifications.Reposts,
            TabNotifications.Mentions,
            TabNotifications.LiveEvents,
        ).map { tabNotificationType ->
            val enabled = remoteMap[tabNotificationType.id]?.jsonPrimitive?.booleanOrNull ?: true
            NotificationSwitchUi(settingsType = tabNotificationType, enabled = enabled)
        }
    }
        .sortedBy { it.settingsType.order }

fun ContentAppSettings.mapAsNotificationsPreferences() =
    notificationsAdditional.toMap().let { remoteMap ->
        listOf(
            Preferences.ReplyRoReply,
            Preferences.HellThread,
            Preferences.DMsFromFollows,
            Preferences.ReactionsFromFollows,
        ).map { preferenceType ->
            val enabled = remoteMap[preferenceType.id]?.jsonPrimitive?.booleanOrNull
                ?: when (preferenceType) {
                    is Preferences.ReactionsFromFollows -> false
                    else -> true
                }
            NotificationSwitchUi(settingsType = preferenceType, enabled = enabled)
        }
    }
