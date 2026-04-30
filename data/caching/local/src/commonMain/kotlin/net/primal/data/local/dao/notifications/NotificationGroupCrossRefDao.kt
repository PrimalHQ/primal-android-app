package net.primal.data.local.dao.notifications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotificationGroupCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(refs: List<NotificationGroupCrossRef>)

    @Query("SELECT COUNT(*) FROM NotificationGroupCrossRef WHERE ownerId = :ownerId AND groupKey = :groupKey")
    suspend fun countByGroup(ownerId: String, groupKey: String): Int

    @Query("DELETE FROM NotificationGroupCrossRef WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)
}
