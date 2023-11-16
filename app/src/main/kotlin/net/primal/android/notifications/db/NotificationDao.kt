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

    @Query("SELECT COUNT(*) FROM NotificationData")
    fun allCount(): Int

    @Query("SELECT * FROM NotificationData ORDER BY createdAt DESC LIMIT 1")
    fun first(): NotificationData?

    @Query("SELECT * FROM NotificationData ORDER BY createdAt ASC LIMIT 1")
    fun last(): NotificationData?

    @Transaction
    @Query("SELECT * FROM NotificationData WHERE seenGloballyAt IS NULL ORDER BY createdAt DESC")
    fun allUnseenNotifications(): Flow<List<Notification>>

    @Transaction
    @Query("SELECT * FROM NotificationData WHERE seenGloballyAt IS NOT NULL ORDER BY createdAt DESC")
    fun allSeenNotificationsPaged(): PagingSource<Int, Notification>

    @Query("UPDATE NotificationData SET seenGloballyAt = :seenAt WHERE seenGloballyAt IS NULL")
    fun markAllUnseenNotificationsAsSeen(seenAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<NotificationData>)
}
