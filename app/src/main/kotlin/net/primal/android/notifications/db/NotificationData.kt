package net.primal.android.notifications.db

import androidx.room.Entity
import net.primal.android.notifications.domain.NotificationType

@Entity(
    primaryKeys = ["ownerId", "createdAt", "type"],
)
data class NotificationData(
    val ownerId: String,
    val createdAt: Long,
    val type: NotificationType,
    val actionUserId: String? = null,
    val actionPostId: String? = null,
    val satsZapped: Int? = null,
)
