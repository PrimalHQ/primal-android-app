package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Upsert
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
