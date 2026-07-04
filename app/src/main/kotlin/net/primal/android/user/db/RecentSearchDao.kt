package net.primal.android.user.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(recentSearch: RecentSearch)

    @Query(
        """
        SELECT * FROM RecentSearch AS RS
        WHERE RS.ownerId IS :ownerId
        ORDER BY RS.lastSearchedAt DESC
        LIMIT :limit
    """,
    )
    fun observeRecentSearches(ownerId: String, limit: Int): Flow<List<RecentSearch>>

    @Query("DELETE FROM RecentSearch WHERE ownerId = :ownerId")
    fun deleteAllByOwnerId(ownerId: String)
}
