package net.primal.data.local.dao.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedPostRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: List<FeedPostRemoteKey>)

    @Query("SELECT * FROM FeedPostRemoteKey WHERE eventId = :eventId AND ownerId = :ownerId LIMIT 1")
    suspend fun findByEventId(ownerId: String, eventId: String): FeedPostRemoteKey?

    @Query(
        """
            SELECT * FROM FeedPostRemoteKey 
            WHERE (eventId = :postId OR eventId = :repostId) AND directive = :directive AND ownerId = :ownerId
        """,
    )
    suspend fun find(
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
    suspend fun findLatestByDirective(ownerId: String, directive: String): FeedPostRemoteKey?

    @Query(
        """
            SELECT cachedAt FROM FeedPostRemoteKey
            WHERE directive = :directive AND ownerId = :ownerId
            ORDER BY cachedAt DESC LIMIT 1
        """,
    )
    suspend fun lastCachedAt(ownerId: String, directive: String): Long?

    @Query("DELETE FROM FeedPostRemoteKey WHERE directive = :directive AND ownerId = :ownerId")
    suspend fun deleteByDirective(ownerId: String, directive: String)

    @Query("DELETE FROM FeedPostRemoteKey WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)

    @Query("DELETE FROM FeedPostRemoteKey WHERE eventId = :eventId")
    suspend fun deleteAllByEventId(eventId: String)
}
