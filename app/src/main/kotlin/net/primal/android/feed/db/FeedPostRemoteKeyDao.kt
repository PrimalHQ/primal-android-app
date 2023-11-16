package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedPostRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: List<FeedPostRemoteKey>)

    @Query(
        """
            SELECT * FROM FeedPostRemoteKey 
            WHERE (eventId = :postId OR eventId = :repostId) AND directive = :directive
        """,
    )
    fun find(
        postId: String?,
        repostId: String?,
        directive: String,
    ): FeedPostRemoteKey?

    @Query("SELECT cachedAt FROM FeedPostRemoteKey WHERE (directive = :directive) ORDER BY cachedAt DESC LIMIT 1")
    fun lastCachedAt(directive: String): Long?

    @Query("DELETE FROM FeedPostRemoteKey WHERE (directive = :directive)")
    fun deleteByDirective(directive: String)
}
