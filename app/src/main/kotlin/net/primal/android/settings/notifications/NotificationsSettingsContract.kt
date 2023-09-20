package net.primal.android.settings.notifications

import kotlinx.serialization.json.JsonObject

interface NotificationsSettingsContract {
    data class UiState(
        val notifications: List<Notification>,
        val error: Throwable? = null
    )

    sealed class UiEvent {
        data class NotificationSettingsChanged(val id: String, val value: Boolean) : UiEvent()
    }

    data class Notification(
        val id: String,
        val textResId: Int,
        val value: Boolean,
        val lightResId: Int,
        val darkResId: Int,
        val group: String
    )
}