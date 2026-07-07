package net.primal.data.local.dao.notes

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.mutes.MutedItemData
import net.primal.data.local.db.FeedPagingSourceDaoReturnTypeConverter
import net.primal.data.local.db.chunkedQuery

@Dao
@DaoReturnTypeConverters(FeedPagingSourceDaoReturnTypeConverter::class)
interface FeedPostDao {

    // The effective observed set for feedQuery's PagingSource is NOT this observedEntities list — codegen
    // adds all @Relation tables of FeedPost on top, and FeedPagingSourceDaoReturnTypeConverter then replaces
    // the whole set with [FeedPostDataCrossRef, MutedItemData, EventUserStats] at PagingSource construction.
    @Transaction
    @RawQuery(observedEntities = [PostData::class, MutedItemData::class, EventUserStats::class])
    fun feedQuery(query: RoomRawQuery): PagingSource<Int, FeedPost>

    @Transaction
    @RawQuery(observedEntities = [PostData::class, MutedItemData::class, EventUserStats::class])
    suspend fun newestFeedPosts(query: RoomRawQuery): List<FeedPost>

    @Transaction
    @RawQuery(observedEntities = [PostData::class, MutedItemData::class, EventUserStats::class])
    suspend fun oldestFeedPosts(query: RoomRawQuery): List<FeedPost>

    @Transaction
    @Query(
        """
        SELECT
            PostData.postId,
            PostData.authorId,
            PostData.createdAt,
            PostData.kind,
            PostData.content,
            PostData.tags,
            PostData.authorMetadataId,
            PostData.hashtags,
            PostData.raw,
            NULL AS repostId,
            NULL AS repostAuthorId,
            NULL AS feedCreatedAt,
            NULL AS isAuthorMuted,
            PostData.replyToPostId,
            PostData.replyToAuthorId
        FROM PostData WHERE postId IN (:postIds)
        """,
    )
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    suspend fun _findAllPostsByIds(postIds: List<String>): List<FeedPost>

    suspend fun findAllPostsByIds(postIds: List<String>): List<FeedPost> =
        postIds.chunkedQuery { _findAllPostsByIds(it) }
}
