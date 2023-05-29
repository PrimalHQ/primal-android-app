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
        WHERE FeedPostDataCrossRef.feedDirective = :feedDirective
        ORDER BY feedCreatedAt DESC
        """
    )
    fun allPostsByFeedDirective(
        feedDirective: String,
    ): PagingSource<Int, FeedPost>

}
