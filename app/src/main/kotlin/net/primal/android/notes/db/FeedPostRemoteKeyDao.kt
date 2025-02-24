package net.primal.android.notes.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedPostRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: List<FeedPostRemoteKey>)

    @Query("SELECT * FROM FeedPostRemoteKey WHERE eventId = :eventId AND ownerId = :ownerId LIMIT 1")
    fun findByEventId(ownerId: String, eventId: String): FeedPostRemoteKey?

    @Query(
        """
            SELECT * FROM FeedPostRemoteKey 
            WHERE (eventId = :postId OR eventId = :repostId) AND directive = :directive AND ownerId = :ownerId
        """,
    )
    fun find(
        postId: String?,
        repostId: String?,
        directive: String,
        ownerId: String,
    ): FeedPostRemoteKey?

    @Query(
        """
            SELECT * FROM FeedPostRemoteKey
            WHERE directive = :directive AND ownerId = :ownerId
            ORDER BY cachedAt DESC LIMIT 1
        """,
    )
    fun findLatestByDirective(ownerId: String, directive: String): FeedPostRemoteKey?

    @Query(
        """
            SELECT cachedAt FROM FeedPostRemoteKey
            WHERE directive = :directive AND ownerId = :ownerId
            ORDER BY cachedAt DESC LIMIT 1
        """,
    )
    fun lastCachedAt(ownerId: String, directive: String): Long?

    @Query("DELETE FROM FeedPostRemoteKey WHERE directive = :directive AND ownerId = :ownerId")
    fun deleteByDirective(ownerId: String, directive: String)

    @Query("DELETE FROM FeedPostRemoteKey WHERE ownerId = :ownerId")
    fun deleteAllByOwnerId(ownerId: String)
}
