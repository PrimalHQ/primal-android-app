package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExplorePopularUserDao {

    @Transaction
    @Query("SELECT * FROM ExplorePopularUserCrossRef ORDER BY position ASC")
    fun observeAll(): Flow<List<ExplorePopularUser>>

    @Upsert
    suspend fun upsertAll(data: List<ExplorePopularUserCrossRef>)

    @Query("DELETE FROM ExplorePopularUserCrossRef")
    suspend fun deleteAll()
}
