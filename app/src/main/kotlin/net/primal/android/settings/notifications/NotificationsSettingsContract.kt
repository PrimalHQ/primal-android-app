package net.primal.android.settings.notifications

import net.primal.android.core.errors.SignatureUiError
import net.primal.android.settings.notifications.ui.NotificationSwitchUi
import net.primal.domain.notifications.NotificationSettingsType
import net.primal.domain.notifications.NotificationSettingsType.Preferences
import net.primal.domain.notifications.NotificationSettingsType.PushNotifications
import net.primal.domain.notifications.NotificationSettingsType.TabNotifications

interface NotificationsSettingsContract {
    data class UiState(
        val pushNotificationsEnabled: Boolean = false,
        val pushNotificationsSettings: List<NotificationSwitchUi<PushNotifications>> = emptyList(),
        val tabNotificationsSettings: List<NotificationSwitchUi<TabNotifications>> = emptyList(),
        val preferencesSettings: List<NotificationSwitchUi<Preferences>> = emptyList(),
        val error: ApiError? = null,
        val signatureError: SignatureUiError? = null,
    ) {
        sealed class ApiError {
            data class FetchAppSettingsError(val cause: Throwable) : ApiError()
            data class UpdateAppSettingsError(val cause: Throwable) : ApiError()
        }
    }

    sealed class UiEvent {
        data object DismissErrors : UiEvent()
        data class NotificationSettingsChanged(val type: NotificationSettingsType, val value: Boolean) : UiEvent()
        data class PushNotificationsToggled(val value: Boolean) : UiEvent()
    }
}
