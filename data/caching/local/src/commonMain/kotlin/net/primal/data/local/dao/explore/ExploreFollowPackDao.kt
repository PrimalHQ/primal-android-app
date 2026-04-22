package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExploreFollowPackDao {

    @Upsert
    suspend fun upsertAll(data: List<ExploreFollowPackCrossRef>)

    @Query("DELETE FROM ExploreFollowPackCrossRef")
    suspend fun deleteAll()
}
