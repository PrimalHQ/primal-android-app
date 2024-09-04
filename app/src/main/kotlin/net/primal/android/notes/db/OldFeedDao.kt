package net.primal.android.notes.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Deprecated("Use FeedDao.")
@Dao
interface OldFeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<OldFeed>)

    @Query("SELECT * FROM OldFeed LIMIT 1")
    fun first(): OldFeed?

    @Query("SELECT * FROM OldFeed")
    fun observeAllFeeds(): Flow<List<OldFeed>>

    @Query("SELECT EXISTS(SELECT 1 FROM OldFeed WHERE directive = :directive)")
    fun observeContainsFeed(directive: String): Flow<Boolean>

    @Query("SELECT * FROM OldFeed WHERE OldFeed.directive = :feedDirective")
    fun observeFeedByDirective(feedDirective: String): Flow<OldFeed?>

    @Query("DELETE FROM OldFeed")
    fun deleteAll()

    @Query("DELETE FROM OldFeed WHERE directive = :directive")
    fun delete(directive: String)
}
