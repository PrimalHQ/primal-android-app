package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<Feed>)

    @Query("SELECT * FROM Feed")
    fun observeAllFeeds(): Flow<List<Feed>>

    @Query("SELECT EXISTS(SELECT 1 FROM Feed WHERE directive = :directive)")
    fun observeContainsFeed(directive: String): Flow<Boolean>

    @Query("SELECT * FROM Feed WHERE Feed.directive = :feedDirective")
    fun observeFeedByDirective(feedDirective: String): Flow<Feed?>

    @Query("DELETE FROM Feed")
    fun deleteAll()

    @Query("DELETE FROM Feed WHERE directive = :directive")
    fun delete(directive: String)
}
