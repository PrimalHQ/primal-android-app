package net.primal.android.notifications.list.ui

import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.notifications.domain.NotificationType
import java.time.Instant

data class NotificationUi(
    val ownerId: String,
    val notificationType: NotificationType,
    val createdAt: Instant,
    val actionUserId: String?,
    val actionUserDisplayName: String?,
    val actionUserInternetIdentifier: String? = null,
    val actionUserPicture: String? = null,
    val actionPost: FeedPostUi? = null,
    val actionUserSatsZapped: Long? = null,
) {
    val uniqueKey = "$notificationType;${createdAt};${actionUserId}"
}
