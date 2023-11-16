package net.primal.android.feed.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import net.primal.android.profile.db.PostUserStats
import net.primal.android.settings.muted.db.MutedUserData

@Dao
interface FeedPostDao {

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, PostUserStats::class])
    fun feedQuery(query: SupportSQLiteQuery): PagingSource<Int, FeedPost>

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, PostUserStats::class])
    fun newestFeedPosts(query: SupportSQLiteQuery): List<FeedPost>

    @Transaction
    @RawQuery(observedEntities = [FeedPost::class, MutedUserData::class, PostUserStats::class])
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
            PostData.hashtags,
            PostData.raw,
            NULL AS repostId,
            NULL AS repostAuthorId,
            NULL AS feedCreatedAt,
            NULL AS isMuted
        FROM PostData WHERE postId = :postId LIMIT 1
        """,
    )
    fun findPostById(postId: String): FeedPost?
}
