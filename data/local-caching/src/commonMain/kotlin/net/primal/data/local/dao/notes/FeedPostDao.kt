package net.primal.data.local.dao.notes

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.profiles.MutedUserData

@Dao
interface FeedPostDao {

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, EventUserStats::class])
    fun feedQuery(query: RoomRawQuery): PagingSource<Int, FeedPost>

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, EventUserStats::class])
    suspend fun newestFeedPosts(query: RoomRawQuery): List<FeedPost>

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, EventUserStats::class])
    suspend fun oldestFeedPosts(query: RoomRawQuery): List<FeedPost>

    @Transaction
    @Query(
        """
        SELECT
            PostData.postId,
            PostData.authorId,
            PostData.createdAt,
            PostData.content,
            PostData.tags,
            PostData.authorMetadataId,
            PostData.hashtags,
            PostData.raw,
            NULL AS repostId,
            NULL AS repostAuthorId,
            NULL AS feedCreatedAt,
            NULL AS isMuted,
            PostData.replyToPostId,
            PostData.replyToAuthorId
        FROM PostData WHERE postId IN (:postIds)
        """,
    )
    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost>
}
