package net.primal.android.notifications.list

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.user.domain.Badges

interface NotificationsContract {

    data class UiState(
        val seenNotifications: Flow<PagingData<NotificationUi>>,
        val unseenNotifications: List<List<NotificationUi>> = emptyList(),
        val activeAccountAvatarUrl: String? = null,
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data object NotificationsSeen : UiEvent()
    }
}
