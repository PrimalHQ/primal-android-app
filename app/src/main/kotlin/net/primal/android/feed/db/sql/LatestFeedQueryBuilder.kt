package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery

class LatestFeedQueryBuilder(
    private val feedDirective: String,
    private val userPubkey: String,
) : FeedQueryBuilder {

    companion object {
        private const val LATEST_BASIC_QUERY = """
            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.raw,
                PostData.authorMetadataId,
                PostData.hashtags,
                NULL AS repostId,
                NULL AS repostAuthorId,
                PostUserStats.liked AS userLiked,
                PostUserStats.replied AS userReplied,
                PostUserStats.reposted AS userReposted,
                PostUserStats.zapped AS userZapped,
                PostData.createdAt AS feedCreatedAt 
            FROM PostData
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            LEFT JOIN PostUserStats ON PostUserStats.postId = PostData.postId AND PostUserStats.userId = ? 
            WHERE FeedPostDataCrossRef.feedDirective = ?

            UNION ALL

            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.raw,
                PostData.authorMetadataId,
                PostData.hashtags,
                RepostData.repostId AS repostId,
                RepostData.authorId AS repostAuthorId,
                PostUserStats.liked AS userLiked,
                PostUserStats.replied AS userReplied,
                PostUserStats.reposted AS userReposted,
                PostUserStats.zapped AS userZapped,
                RepostData.createdAt AS feedCreatedAt
            FROM RepostData
            JOIN PostData ON RepostData.postId = PostData.postId
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = RepostData.repostId
            LEFT JOIN PostUserStats ON PostUserStats.postId = PostData.postId AND PostUserStats.userId = ?
            WHERE FeedPostDataCrossRef.feedDirective = ?
        """
    }

    override fun feedQuery(): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC",
            bindArgs = arrayOf(userPubkey, feedDirective, userPubkey, feedDirective),
        )
    }

    override fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC LIMIT ?",
            bindArgs = arrayOf(userPubkey, feedDirective, userPubkey, feedDirective, limit),
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt ASC LIMIT ?",
            bindArgs = arrayOf(userPubkey, feedDirective, userPubkey, feedDirective, limit),
        )
    }
}
