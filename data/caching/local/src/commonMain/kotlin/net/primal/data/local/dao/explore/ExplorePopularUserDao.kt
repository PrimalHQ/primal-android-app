package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExplorePopularUserDao {

    @Query("SELECT * FROM ExplorePopularUserData ORDER BY position ASC")
    fun observeAll(): Flow<List<ExplorePopularUserData>>

    @Upsert
    suspend fun upsertAll(data: List<ExplorePopularUserData>)

    @Query("DELETE FROM ExplorePopularUserData")
    suspend fun deleteAll()
}
