package net.primal.data.local.dao.explore

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface FollowPackListCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connect(data: List<FollowPackListCrossRef>)

    @Query("SELECT * FROM FollowPackListCrossRef ORDER BY position DESC LIMIT 1")
    suspend fun findLast(): FollowPackListCrossRef?

    @Query("DELETE FROM FollowPackListCrossRef")
    suspend fun deleteConnections()
}
