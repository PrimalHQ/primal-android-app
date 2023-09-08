package net.primal.android.notifications.list

interface NotificationsContract {

    data class UiState(
        val loading: Boolean = true,
        val activeAccountAvatarUrl: String? = null,
    )
}
