package net.primal.android.feeds.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.primal.android.feeds.domain.FeedSpecKind

@Dao
interface FeedDao {

    @Query("SELECT * FROM Feed WHERE specKind = :specKind")
    fun getAllFeedsBySpecKind(specKind: FeedSpecKind): List<Feed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<Feed>)

    @Query("SELECT * FROM Feed WHERE specKind = :specKind LIMIT 1")
    fun first(specKind: FeedSpecKind): Feed?

    @Query("SELECT * FROM Feed")
    fun observeAllFeeds(): Flow<List<Feed>>

    @Query("SELECT * FROM Feed WHERE specKind = :specKind")
    fun observeAllFeeds(specKind: FeedSpecKind): Flow<List<Feed>>

    @Query("SELECT EXISTS(SELECT 1 FROM Feed WHERE spec = :spec)")
    fun observeContainsFeed(spec: String): Flow<Boolean>

    @Query("SELECT * FROM Feed WHERE Feed.spec = :spec")
    fun observeFeedBySpec(spec: String): Flow<Feed?>

    @Query("DELETE FROM Feed WHERE specKind = :specKind")
    fun deleteAll(specKind: FeedSpecKind)

    @Query("DELETE FROM Feed WHERE spec = :spec")
    fun delete(spec: String)
}
