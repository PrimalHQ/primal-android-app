package net.primal.android.settings.notifications

import net.primal.android.notifications.domain.NotificationType

interface NotificationsSettingsContract {
    data class UiState(
        val notificationSwitches: List<NotificationSwitchUi> = emptyList(),
        val error: ApiError? = null,
    ) {
        sealed class ApiError {
            data class FetchAppSettingsError(val cause: Throwable) : ApiError()
            data class UpdateAppSettingsError(val cause: Throwable) : ApiError()
        }
    }

    sealed class UiEvent {
        data class NotificationSettingChanged(
            val type: NotificationType,
            val value: Boolean,
        ) : UiEvent()

        data object DismissErrors : UiEvent()
    }
}
