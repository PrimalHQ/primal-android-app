package net.primal.android.feed.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery


@Dao
interface FeedPostDao {

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class])
    fun feedQuery(query: SupportSQLiteQuery): PagingSource<Int, FeedPost>

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class])
    fun newestFeedPosts(query: SupportSQLiteQuery): List<FeedPost>

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class])
    fun oldestFeedPosts(query: SupportSQLiteQuery): List<FeedPost>

    @Transaction
    @Query(
        """
        SELECT
            PostData.postId,
            PostData.authorId,
            PostData.createdAt,
            PostData.content,
            PostData.authorMetadataId,
            PostData.referencePostId,
            PostData.referencePostAuthorId,
            PostData.raw,
            NULL AS repostId,
            NULL AS repostAuthorId,
            NULL AS feedCreatedAt
        FROM PostData WHERE postId = :postId LIMIT 1
        """
    )
    fun findPostById(postId: String): FeedPost

}
