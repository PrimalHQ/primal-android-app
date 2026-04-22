package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExploreFollowPackDao {

    @Upsert
    suspend fun upsertAll(data: List<ExploreFollowPackData>)

    @Query("DELETE FROM ExploreFollowPackData")
    suspend fun deleteAll()
}
