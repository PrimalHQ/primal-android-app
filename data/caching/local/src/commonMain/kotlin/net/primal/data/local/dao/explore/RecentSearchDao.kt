package net.primal.data.local.dao.explore

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: RecentSearch)

    @Query("SELECT * FROM RecentSearch WHERE ownerId = :ownerId ORDER BY lastSearchedAt DESC LIMIT :limit")
    fun observeRecentSearches(ownerId: String, limit: Int): Flow<List<RecentSearch>>
}
