package net.primal.android.notifications.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.notifications.domain.NotificationType

@Entity
data class NotificationData(
    @PrimaryKey
    val notificationId: String,
    val ownerId: String,
    val createdAt: Long,
    val type: NotificationType,
    val seenGloballyAt: Long? = null,
    val actionUserId: String? = null,
    val actionPostId: String? = null,
    val satsZapped: Long? = null,
)
