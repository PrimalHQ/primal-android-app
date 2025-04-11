package net.primal.android.settings.notifications

import net.primal.android.core.errors.SignatureUiError
import net.primal.domain.notifications.NotificationType

interface NotificationsSettingsContract {
    data class UiState(
        val enabledPushNotifications: Boolean = false,
        val notificationSwitches: List<NotificationSwitchUi> = emptyList(),
        val error: ApiError? = null,
        val signatureError: SignatureUiError? = null,
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
