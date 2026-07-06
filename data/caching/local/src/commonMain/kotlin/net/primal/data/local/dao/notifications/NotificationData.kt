package net.primal.data.local.dao.notifications

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import net.primal.domain.notifications.NotificationType

@Entity(
    indices = [
        Index(value = ["ownerId", "createdAt"]),
    ],
)
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
    val reaction: String? = null,
)
