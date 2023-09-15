package net.primal.android.settings.notifications

import kotlinx.serialization.json.JsonObject

interface NotificationsSettingsContract {
    data class UiState(
        val nots: JsonObject
    )

    sealed class UiEvent {
        data class NotificationOptionsChanged(val nots: JsonObject): UiEvent()
    }
}