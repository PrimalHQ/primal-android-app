package net.primal.android.explore.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendingTopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<TrendingTopic>)

    @Query("DELETE FROM TrendingTopic")
    fun deleteAll()

    @Query("SELECT * FROM TrendingTopic ORDER BY score DESC")
    fun allSortedByScore(): Flow<List<TrendingTopic>>
}
