package net.primal.android.notifications.list

import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.user.domain.Badges

interface NotificationsContract {

    data class UiState(
        val loading: Boolean = false,
        val activeAccountAvatarUrl: String? = null,
        val badges: Badges = Badges(),
        val notifications: List<NotificationUi> = emptyList(),
    )

    sealed class UiEvent {
        data object NotificationsSeen : UiEvent()
    }
}
