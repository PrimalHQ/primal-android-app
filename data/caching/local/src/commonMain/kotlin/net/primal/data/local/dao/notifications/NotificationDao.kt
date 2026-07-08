package net.primal.data.local.dao.notifications

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface NotificationDao {

    @Transaction
    @Query(
        """
            SELECT n.* FROM NotificationData n
            INNER JOIN NotificationGroupCrossRef g
                ON n.notificationId = g.notificationId AND n.ownerId = g.ownerId
            WHERE n.ownerId = :ownerId
              AND g.groupKey = :groupKey
              AND n.seenGloballyAt IS NOT NULL
            ORDER BY n.createdAt DESC
        """,
    )
    fun seenByGroupPaged(ownerId: String, groupKey: String): PagingSource<Int, Notification>

    @Transaction
    @Query(
        """
            SELECT n.* FROM NotificationData n
            INNER JOIN NotificationGroupCrossRef g
                ON n.notificationId = g.notificationId AND n.ownerId = g.ownerId
            WHERE n.ownerId = :ownerId
              AND g.groupKey = :groupKey
              AND n.seenGloballyAt IS NULL
            ORDER BY n.createdAt DESC
        """,
    )
    fun unseenByGroup(ownerId: String, groupKey: String): Flow<List<Notification>>

    @Query(
        """
            SELECT n.* FROM NotificationData n
            INNER JOIN NotificationGroupCrossRef g
                ON n.notificationId = g.notificationId AND n.ownerId = g.ownerId
            WHERE n.ownerId = :ownerId AND g.groupKey = :groupKey
            ORDER BY n.createdAt DESC LIMIT 1
        """,
    )
    suspend fun firstByGroup(ownerId: String, groupKey: String): NotificationData?

    @Query(
        """
            SELECT n.* FROM NotificationData n
            INNER JOIN NotificationGroupCrossRef g
                ON n.notificationId = g.notificationId AND n.ownerId = g.ownerId
            WHERE n.ownerId = :ownerId AND g.groupKey = :groupKey
            ORDER BY n.createdAt ASC LIMIT 1
        """,
    )
    suspend fun lastByGroup(ownerId: String, groupKey: String): NotificationData?

    @Query("UPDATE NotificationData SET seenGloballyAt = :seenAt WHERE seenGloballyAt IS NULL AND ownerId = :ownerId")
    suspend fun markAllUnseenNotificationsAsSeen(ownerId: String, seenAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<NotificationData>)

    @Query("DELETE FROM NotificationData WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)
}
