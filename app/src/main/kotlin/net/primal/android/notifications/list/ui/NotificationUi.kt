package net.primal.android.notifications.list.ui

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.premium.legend.LegendaryCustomization

data class NotificationUi(
    val notificationId: String,
    val ownerId: String,
    val notificationType: NotificationType,
    val createdAt: Instant,
    val actionUserId: String?,
    val actionUserDisplayName: String?,
    val actionUserInternetIdentifier: String? = null,
    val actionUserAvatarCdnImage: CdnImage? = null,
    val actionUserLegendaryCustomization: LegendaryCustomization? = null,
    val actionPost: FeedPostUi? = null,
    val actionUserSatsZapped: Long? = null,
)
