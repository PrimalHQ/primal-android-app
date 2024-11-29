package net.primal.android.notifications.list

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.user.domain.Badges

interface NotificationsContract {

    data class UiState(
        val seenNotifications: Flow<PagingData<NotificationUi>>,
        val unseenNotifications: List<List<NotificationUi>> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data object NotificationsSeen : UiEvent()
    }
}
