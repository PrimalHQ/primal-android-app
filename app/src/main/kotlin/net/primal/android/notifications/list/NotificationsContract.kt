package net.primal.android.notifications.list

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.notifications.list.ui.NotificationUi
import net.primal.android.user.domain.Badges

interface NotificationsContract {

    data class UiState(
        val walletConnected: Boolean = false,
        val defaultZapAmount: ULong? = null,
        val zapOptions: List<ULong> = emptyList(),
        val seenNotifications: Flow<PagingData<NotificationUi>>,
        val unseenNotifications: List<List<NotificationUi>> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val badges: Badges = Badges(),
        val error: NotificationsError? = null,
    ) {
        sealed class NotificationsError {
            data class MissingLightningAddress(val cause: Throwable) : NotificationsError()
            data class InvalidZapRequest(val cause: Throwable) : NotificationsError()
            data class FailedToPublishZapEvent(val cause: Throwable) : NotificationsError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : NotificationsError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : NotificationsError()
            data class MissingRelaysConfiguration(val cause: Throwable) : NotificationsError()
        }
    }

    sealed class UiEvent {
        data object NotificationsSeen : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String,
        ) : UiEvent()
        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val zapAmount: ULong?,
            val zapDescription: String?,
        ) : UiEvent()
    }
}
