package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery

class LatestFeedQueryBuilder(
    private val feedDirective: String,
) : FeedQueryBuilder {

    companion object {
        private const val LATEST_BASIC_QUERY = """
            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.referencePostId,
                PostData.referencePostAuthorId,
                RepostData.repostId AS repostId,
                RepostData.authorId AS repostAuthorId,
                COALESCE(RepostData.createdAt, PostData.createdAt) AS feedCreatedAt
            FROM PostData
            LEFT JOIN RepostData ON RepostData.postId = PostData.postId
            INNER JOIN FeedPostDataCrossRef ON PostData.postId = FeedPostDataCrossRef.postId
            WHERE FeedPostDataCrossRef.feedDirective = ?
        """
    }

    override fun feedQuery(): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC",
            bindArgs = arrayOf(feedDirective),
        )
    }

    override fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC LIMIT ?",
            bindArgs = arrayOf(feedDirective, limit),
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt ASC LIMIT ?",
            bindArgs = arrayOf(feedDirective, limit),
        )
    }
}
