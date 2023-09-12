package net.primal.android.notifications.list

import net.primal.android.user.domain.Badges

interface NotificationsContract {

    data class UiState(
        val loading: Boolean = true,
        val activeAccountAvatarUrl: String? = null,
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data object NotificationsSeen : UiEvent()
    }
}
