package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedPostSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: FeedPostSync)

    @Query(
        """
        SELECT * FROM FeedPostSync 
        WHERE feedDirective = :feedDirective AND timestamp >= :since
        ORDER BY timestamp DESC
        LIMIT 1
        """,
    )
    fun observeFeedDirective(feedDirective: String, since: Long): Flow<FeedPostSync>
}
