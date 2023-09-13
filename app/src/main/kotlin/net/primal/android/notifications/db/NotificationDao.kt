package net.primal.android.notifications.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Upsert
    fun upsertAll(data: List<NotificationData>)

    @Query("DELETE FROM NotificationData WHERE userId = :userId")
    fun deleteAlL(userId: String)

    @Query("SELECT COUNT(*) FROM NotificationData")
    fun count(): Int

    @Query("SELECT * FROM NotificationData ORDER BY createdAt DESC")
    fun allSortedByCreatedAtDesc(): Flow<List<Notification>>
}
