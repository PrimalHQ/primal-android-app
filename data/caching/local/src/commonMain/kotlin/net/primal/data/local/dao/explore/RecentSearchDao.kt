package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: RecentSearch)

    @Query("SELECT * FROM RecentSearch WHERE ownerId = :ownerId ORDER BY lastSearchedAt DESC LIMIT :limit")
    fun observeRecentSearches(ownerId: String, limit: Int): Flow<List<RecentSearch>>
}
