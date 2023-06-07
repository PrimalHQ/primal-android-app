package net.primal.android.feed.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FeedPostDao {

    @Transaction
    @Query(
        """
        SELECT FeedPostData.feedCreatedAt FROM FeedPostData
        INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
        WHERE FeedPostDataCrossRef.feedDirective = :feedDirective
        ORDER BY feedCreatedAt DESC
        LIMIT 1
        """
    )
    fun newestPostFeedCreatedAt(feedDirective: String): Long?

    @Transaction
    @Query(
        """
        SELECT FeedPostData.* FROM FeedPostData
        INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
        WHERE FeedPostDataCrossRef.feedDirective = :feedDirective
        ORDER BY feedCreatedAt DESC
        """
    )
    fun allPostsByFeedDirectiveOrderByCreatedAt(
        feedDirective: String,
    ): PagingSource<Int, FeedPost>

    @Transaction
    @Query(
        """
        SELECT FeedPostData.* FROM FeedPostData
        INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
        INNER JOIN PostStats ON FeedPostData.postId = PostStats.postId
        WHERE FeedPostDataCrossRef.feedDirective = :feedDirective
        ORDER BY PostStats.score DESC
        """
    )
    fun allPostsByFeedDirectiveOrderByScore(
        feedDirective: String,
    ): PagingSource<Int, FeedPost>

    @Transaction
    @Query(
        """
        SELECT FeedPostData.* FROM FeedPostData
        INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
        INNER JOIN PostStats ON FeedPostData.postId = PostStats.postId
        WHERE FeedPostDataCrossRef.feedDirective = :feedDirective
        ORDER BY PostStats.score24h DESC
        """
    )
    fun allPostsByFeedDirectiveOrderByScore24h(
        feedDirective: String,
    ): PagingSource<Int, FeedPost>

    @Transaction
    @Query(
        """
        SELECT FeedPostData.* FROM FeedPostData
        INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
        INNER JOIN PostStats ON FeedPostData.postId = PostStats.postId
        WHERE FeedPostDataCrossRef.feedDirective = :feedDirective
        ORDER BY PostStats.satsZapped DESC
        """
    )
    fun allPostsByFeedDirectiveOrderBySatsZapped(
        feedDirective: String,
    ): PagingSource<Int, FeedPost>


}
