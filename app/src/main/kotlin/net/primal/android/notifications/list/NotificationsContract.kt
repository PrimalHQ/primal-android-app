package net.primal.android.notifications.list

import net.primal.android.notifications.db.Notification
import net.primal.android.user.domain.Badges

interface NotificationsContract {

    data class UiState(
        val loading: Boolean = true,
        val activeAccountAvatarUrl: String? = null,
        val badges: Badges = Badges(),
        val notifications: List<Notification> = emptyList(),
    )

    sealed class UiEvent {
        data object NotificationsSeen : UiEvent()
    }
}
