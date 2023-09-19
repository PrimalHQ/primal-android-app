package net.primal.android.settings.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.R
import javax.inject.Inject

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(

) : ViewModel() {
    private val _state = MutableStateFlow(
        NotificationsSettingsContract.UiState(
            mapOf(
                "CORE NOTIFICATIONS" to listOf(
                    NotificationsSettingsContract.Notification(
                        id = "NEW_USER_FOLLOWED_YOU",
                        name = "new user followed you",
                        lightResId = R.drawable.notification_type_new_user_followed_you_light,
                        darkResId = R.drawable.notification_type_new_user_followed_you_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "USER_UNFOLLOWED_YOU",
                        name = "user unfollowed you",
                        lightResId = R.drawable.notification_type_user_unfollowed_you_light,
                        darkResId = R.drawable.notification_type_user_unfollowed_you_dark,
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_ZAPPED",
                        name = "your post was zapped",
                        lightResId = R.drawable.notification_type_your_post_was_zapped_light,
                        darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_LIKED",
                        name = "your post was liked",
                        lightResId = R.drawable.notification_type_your_post_was_liked_light,
                        darkResId = R.drawable.notification_type_your_post_was_liked_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_REPOSTED",
                        name = "your post was reposted",
                        lightResId = R.drawable.notification_type_your_post_was_reposted_light,
                        darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_REPLIED_TO",
                        name = "your post was replied to",
                        lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
                        darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOU_WERE_MENTIONED_IN_POST",
                        name = "you were mentioned",
                        lightResId = R.drawable.notification_type_new_user_followed_you_light, // TODO: update when new icons become available
                        darkResId = R.drawable.notification_type_new_user_followed_you_dark, // TODO: update when new icons become available
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_MENTIONED_IN_POST",
                        name = "your post was mentioned",
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light, // TODO: update when new icons become available
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark, // TODO: update when new icons become available
                        value = true
                    )
                ),
                "A NOTE YOU WERE MENTIONED IN WAS" to listOf(
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED",
                        name = "zapped",
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_LIKED",
                        name = "liked",
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED",
                        name = "reposted",
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_dark,
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO",
                        name = "replied to",
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_dark,
                        value = true
                    )
                ),
                "A NOTE YOUR NOTE WAS MENTIONED IN WAS" to listOf(
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED",
                        name = "zapped",
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_dark,
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED",
                        name = "liked",
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark,
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED",
                        name = "reposted",
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_dark,
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO",
                        name = "replied to",
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_dark,
                        value = false
                    )
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

    init {}

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is NotificationsSettingsContract.UiEvent.NotificationSettingsChanged -> updateNotificationsSettings(
                    id = it.id, value = it.value
                )
            }
        }
    }

    private fun updateNotificationsSettings(id: String, value: Boolean) {

    }
}