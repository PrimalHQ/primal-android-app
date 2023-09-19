package net.primal.android.settings.notifications

import kotlinx.serialization.json.JsonObject

interface NotificationsSettingsContract {
    data class UiState(
        val notifications: Map<String, List<Notification>>
    )

    sealed class UiEvent {
        data class NotificationSettingsChanged(val id: String, val value: Boolean) : UiEvent()
    }

    data class Notification(
        val id: String,
        val name: String,
        val value: Boolean,
        val lightResId: Int,
        val darkResId: Int
    )
}