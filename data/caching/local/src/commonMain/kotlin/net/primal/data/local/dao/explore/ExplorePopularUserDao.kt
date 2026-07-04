package net.primal.data.local.dao.explore

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
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
