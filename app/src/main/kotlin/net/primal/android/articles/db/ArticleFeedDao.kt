package net.primal.android.articles.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleFeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<ArticleFeed>)

    @Query("SELECT * FROM ArticleFeed LIMIT 1")
    fun first(): ArticleFeed?

    @Query("SELECT * FROM ArticleFeed")
    fun observeAllFeeds(): Flow<List<ArticleFeed>>

    @Query("SELECT EXISTS(SELECT 1 FROM ArticleFeed WHERE spec = :spec)")
    fun observeContainsFeed(spec: String): Flow<Boolean>

    @Query("SELECT * FROM ArticleFeed WHERE ArticleFeed.spec = :feedDirective")
    fun observeFeedBySpec(feedDirective: String): Flow<ArticleFeed?>

    @Query("DELETE FROM ArticleFeed")
    fun deleteAll()

    @Query("DELETE FROM ArticleFeed WHERE spec = :spec")
    fun delete(spec: String)
}
