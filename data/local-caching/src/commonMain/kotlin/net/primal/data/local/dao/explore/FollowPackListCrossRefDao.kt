package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FollowPackListCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connect(data: List<FollowPackListCrossRef>)

    @Query("SELECT * FROM FollowPackListCrossRef ORDER BY position DESC LIMIT 1")
    suspend fun findLast(): FollowPackListCrossRef?

    @Query("DELETE FROM FollowPackListCrossRef")
    suspend fun deleteConnections()
}
