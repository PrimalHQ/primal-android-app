package net.primal.android.notifications.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Upsert
    fun upsertAll(data: List<Notification>)

    @Query("DELETE FROM Notification WHERE userId = :userId")
    fun deleteAlL(userId: String)

    @Query("SELECT * FROM Notification ORDER BY createdAt DESC")
    fun allSortedByCreatedAtDesc(): Flow<List<Notification>>
}
