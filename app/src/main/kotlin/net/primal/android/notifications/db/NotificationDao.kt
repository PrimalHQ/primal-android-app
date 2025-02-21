package net.primal.android.notifications.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT COUNT(*) FROM NotificationData WHERE ownerId = :ownerId")
    fun allCount(ownerId: String): Int

    @Query("SELECT * FROM NotificationData WHERE ownerId = :ownerId ORDER BY createdAt DESC LIMIT 1")
    fun first(ownerId: String): NotificationData?

    @Query("SELECT * FROM NotificationData WHERE ownerId = :ownerId ORDER BY createdAt ASC LIMIT 1")
    fun last(ownerId: String): NotificationData?

    @Transaction
    @Query("SELECT * FROM NotificationData WHERE seenGloballyAt IS NULL AND ownerId = :ownerId ORDER BY createdAt DESC")
    fun allUnseenNotifications(ownerId: String): Flow<List<Notification>>

    @Transaction
    @Query("""
            SELECT * FROM NotificationData
            WHERE seenGloballyAt IS NOT NULL AND ownerId = :ownerId 
            ORDER BY createdAt DESC
        """,
    )
    fun allSeenNotificationsPaged(ownerId: String): PagingSource<Int, Notification>

    @Query("UPDATE NotificationData SET seenGloballyAt = :seenAt WHERE seenGloballyAt IS NULL AND ownerId = :ownerId")
    fun markAllUnseenNotificationsAsSeen(ownerId: String, seenAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<NotificationData>)
}
