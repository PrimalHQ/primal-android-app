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
                NULL AS repostId,
                NULL AS repostAuthorId,
                PostData.createdAt AS feedCreatedAt 
            FROM PostData
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            WHERE FeedPostDataCrossRef.feedDirective = ?

            UNION ALL

            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.referencePostId,
                PostData.referencePostAuthorId,
                RepostData.repostId AS repostId,
                RepostData.authorId AS repostAuthorId,
                RepostData.createdAt AS feedCreatedAt
            FROM RepostData
            JOIN PostData ON RepostData.postId = PostData.postId
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = RepostData.repostId
            WHERE FeedPostDataCrossRef.feedDirective = ?
        """
    }

    override fun feedQuery(): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC",
            bindArgs = arrayOf(feedDirective, feedDirective),
        )
    }

    override fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt DESC LIMIT ?",
            bindArgs = arrayOf(feedDirective, feedDirective, limit),
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            query = "$LATEST_BASIC_QUERY ORDER BY feedCreatedAt ASC LIMIT ?",
            bindArgs = arrayOf(feedDirective, feedDirective, limit),
        )
    }
}
