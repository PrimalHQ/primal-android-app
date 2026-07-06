package net.primal.data.local.dao.notifications

import androidx.room3.Entity
import androidx.room3.Index

@Entity(
    tableName = "NotificationGroupCrossRef",
    primaryKeys = ["ownerId", "groupKey", "notificationId"],
    indices = [Index(value = ["notificationId", "ownerId"])],
)
data class NotificationGroupCrossRef(
    val notificationId: String,
    val ownerId: String,
    val groupKey: String,
)
