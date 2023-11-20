package net.primal.android.notifications.list.ui

import java.time.Instant
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.notifications.domain.NotificationType

data class NotificationUi(
    val ownerId: String,
    val notificationType: NotificationType,
    val createdAt: Instant,
    val actionUserId: String?,
    val actionUserDisplayName: String?,
    val actionUserInternetIdentifier: String? = null,
    val actionUserAvatarUrl: String? = null,
    val actionUserAvatarVariants: List<CdnResourceVariant> = emptyList(),
    val actionPost: FeedPostUi? = null,
    val actionUserSatsZapped: Long? = null,
) {
    val uniqueKey = "$notificationType;$createdAt;$actionUserId"
}
