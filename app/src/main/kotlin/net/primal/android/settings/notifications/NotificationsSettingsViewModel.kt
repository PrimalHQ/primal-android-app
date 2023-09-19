package net.primal.android.settings.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.R
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val activeAccountStore: ActiveAccountStore
) : ViewModel() {
    private val _state = MutableStateFlow(
        NotificationsSettingsContract.UiState(
            notifications = listOf(
                NotificationsSettingsContract.Notification(
                    id = "NEW_USER_FOLLOWED_YOU",
                    textResId = R.string.settings_notifications_new_user_followed_you_text,
                    lightResId = R.drawable.notification_type_new_user_followed_you_light,
                    darkResId = R.drawable.notification_type_new_user_followed_you_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "YOUR_POST_WAS_ZAPPED",
                    textResId = R.string.settings_notifications_your_post_was_zapped_text,
                    lightResId = R.drawable.notification_type_your_post_was_zapped_light,
                    darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "YOUR_POST_WAS_LIKED",
                    textResId = R.string.settings_notifications_your_post_was_liked_text,
                    lightResId = R.drawable.notification_type_your_post_was_liked_light,
                    darkResId = R.drawable.notification_type_your_post_was_liked_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "YOUR_POST_WAS_REPOSTED",
                    textResId = R.string.settings_notifications_your_post_was_reposted_text,
                    lightResId = R.drawable.notification_type_your_post_was_reposted_light,
                    darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "YOUR_POST_WAS_REPLIED_TO",
                    textResId = R.string.settings_notifications_your_post_was_replied_to_text,
                    lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
                    darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "YOU_WERE_MENTIONED_IN_POST",
                    textResId = R.string.settings_notifications_you_were_mentioned_text,
                    lightResId = R.drawable.notification_type_you_were_mentioned_in_a_post_light,
                    darkResId = R.drawable.notification_type_you_were_mentioned_in_a_post_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "YOUR_POST_WAS_MENTIONED_IN_POST",
                    textResId = R.string.settings_notifications_your_post_was_mentioned_text,
                    lightResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_light,
                    darkResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_dark,
                    group = "CORE NOTIFICATIONS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED",
                    textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_zapped_text,
                    lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_light,
                    darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_dark,
                    group = "A NOTE YOU WERE MENTIONED IN WAS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOU_WERE_MENTIONED_IN_WAS_LIKED",
                    textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_liked_text,
                    lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_light,
                    darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark,
                    group = "A NOTE YOU WERE MENTIONED IN WAS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED",
                    textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_reposted_text,
                    lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_light,
                    darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_dark,
                    group = "A NOTE YOU WERE MENTIONED IN WAS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO",
                    textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_replied_to_text,
                    lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_light,
                    darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_dark,
                    group = "A NOTE YOU WERE MENTIONED IN WAS",
                    value = true
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED",
                    textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_zapped_text,
                    lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_light,
                    darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_dark,
                    group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                    value = false
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED",
                    textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_liked_text,
                    lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light,
                    darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark,
                    group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                    value = false
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED",
                    textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_reposted_text,
                    lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_light,
                    darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_dark,
                    group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                    value = false
                ),
                NotificationsSettingsContract.Notification(
                    id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO",
                    textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_replied_to_text,
                    lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_light,
                    darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_dark,
                    group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                    value = false
                )
            )
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: NotificationsSettingsContract.UiState.() -> NotificationsSettingsContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _event: MutableSharedFlow<NotificationsSettingsContract.UiEvent> =
        MutableSharedFlow()

    fun setEvent(event: NotificationsSettingsContract.UiEvent) =
        viewModelScope.launch { _event.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is NotificationsSettingsContract.UiEvent.NotificationSettingsChanged -> updateNotificationsSettings(
                    id = it.id, value = it.value
                )
            }
        }
    }

    private fun observeActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount
            .mapNotNull { it.appSettings }
            .collect { appSettings ->
                val dbNotifications = appSettings.notifications.toMap()
                val localNotifications = state.value.notifications.toMutableList()

                for (dbn in dbNotifications) {
                    val localNotification = localNotifications.find { it.id == dbn.key }
                    if (localNotification != null) {
                        val index = localNotifications.indexOf(localNotification)
                        localNotifications[index] =
                            localNotification.copy(value = dbn.value.jsonPrimitive.boolean)
                    }
                }

                setState { copy(notifications = localNotifications) }
            }
    }

    private suspend fun updateNotificationsSettings(id: String, value: Boolean) {
        try {
            val newNotificationsList = state.value.notifications.toMutableList()

            val existingSetting = newNotificationsList.first { it.id == id }
            val existingSettingIndex = newNotificationsList.indexOf(existingSetting)
            newNotificationsList[existingSettingIndex] = existingSetting.copy(value = value)

            val notificationsJsonObject = JsonObject(content = newNotificationsList.associate {
                it.id to JsonPrimitive(
                    value = it.value
                )
            })

            settingsRepository.updateAndPersistNotifications(
                userId = activeAccountStore.activeUserId(),
                notifications = notificationsJsonObject
            )
            setState { copy(notifications = newNotificationsList) }
        } catch (error: WssException) {
            setState { copy(error = error) }
        }
    }
}