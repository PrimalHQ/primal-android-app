package net.primal.android.notifications.list.ui

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.notifications.domain.NotificationType

data class NotificationUi(
    val ownerId: String,
    val notificationType: NotificationType,
    val createdAt: Instant,
    val actionUserId: String?,
    val actionUserDisplayName: String?,
    val actionUserInternetIdentifier: String? = null,
    val actionUserAvatarCdnImage: CdnImage? = null,
    val actionPost: FeedPostUi? = null,
    val actionUserSatsZapped: Long? = null,
) {
    val uniqueKey = "$notificationType;$createdAt;$actionUserId"
}
