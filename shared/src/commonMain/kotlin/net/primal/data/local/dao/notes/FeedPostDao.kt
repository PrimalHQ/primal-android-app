package net.primal.data.local.dao.notes

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FeedPostDao {

//    @Transaction
//    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, EventUserStats::class])
//    fun feedQuery(query: SupportSQLiteQuery): PagingSource<Int, FeedPost>
//
//    @Transaction
//    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, EventUserStats::class])
//    fun newestFeedPosts(query: SupportSQLiteQuery): List<FeedPost>
//
//    @Transaction
//    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, EventUserStats::class])
//    fun oldestFeedPosts(query: SupportSQLiteQuery): List<FeedPost>

    @Transaction
    @Query(
        """
        SELECT
            PostData.postId,
            PostData.authorId,
            PostData.createdAt,
            PostData.content,
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
