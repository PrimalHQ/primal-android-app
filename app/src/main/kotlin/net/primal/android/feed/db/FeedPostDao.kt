package net.primal.android.feed.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedPostDao {

    @Query("SELECT COUNT(*) FROM FeedPostData")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM FeedPostData ORDER BY feedCreatedAt DESC")
    fun allPostsPaged(): PagingSource<Int, FeedPost>

}