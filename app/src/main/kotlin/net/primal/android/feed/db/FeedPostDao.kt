package net.primal.android.feed.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction

@Dao
interface FeedPostDao {

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
        SELECT * FROM FeedPostData
        INNER JOIN FeedPostDataCrossRef ON FeedPostData.postId = FeedPostDataCrossRef.postId
        WHERE FeedPostDataCrossRef.feedId = :feedId
        ORDER BY feedCreatedAt DESC
        """
    )
    fun allPostsByFeedId(
        feedId: String,
    ): PagingSource<Int, FeedPost>

}
