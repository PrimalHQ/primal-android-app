package net.primal.android.feed.db

import androidx.paging.PagingSource
import androidx.room.Dao
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

}
