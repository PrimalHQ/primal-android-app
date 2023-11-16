package net.primal.android.explore.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendingHashtagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<TrendingHashtag>)

    @Query("DELETE FROM TrendingHashtag")
    fun deleteAlL()

    @Query("SELECT * FROM TrendingHashtag ORDER BY score DESC")
    fun allSortedByScore(): Flow<List<TrendingHashtag>>
}
