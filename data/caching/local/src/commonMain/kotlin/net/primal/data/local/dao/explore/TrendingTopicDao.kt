package net.primal.data.local.dao.explore

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendingTopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<TrendingTopic>)

    @Query("DELETE FROM TrendingTopic")
    suspend fun deleteAll()

    @Query("SELECT * FROM TrendingTopic ORDER BY score DESC")
    fun allSortedByScore(): Flow<List<TrendingTopic>>
}
