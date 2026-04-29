package net.primal.android.main.notifications

import net.primal.android.user.domain.Badges
import net.primal.domain.notifications.NotificationGroup

interface NotificationsContract {

    data class UiState(
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data class NotificationsSeen(val group: NotificationGroup) : UiEvent()
    }
}
