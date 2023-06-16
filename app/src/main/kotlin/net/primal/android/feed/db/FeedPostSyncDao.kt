package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedPostSyncDao {

    @Upsert
    fun upsert(data: FeedPostSync)

    @Query("SELECT * FROM FeedPostSync WHERE feedDirective = :feedDirective AND timestamp >= :since")
    fun observeFeedDirective(feedDirective: String, since: Long): Flow<FeedPostSync>

}
