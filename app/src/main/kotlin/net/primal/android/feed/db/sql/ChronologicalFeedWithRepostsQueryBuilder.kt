package net.primal.android.feed.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery

class ChronologicalFeedWithRepostsQueryBuilder(
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
                NoteUserStats.liked AS userLiked,
                NoteUserStats.replied AS userReplied,
                NoteUserStats.reposted AS userReposted,
                NoteUserStats.zapped AS userZapped,
                PostData.createdAt AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM PostData
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            LEFT JOIN NoteUserStats ON NoteUserStats.postId = PostData.postId AND NoteUserStats.userId = ?
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE FeedPostDataCrossRef.feedDirective = ? AND isMuted = 0

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
                NoteUserStats.liked AS userLiked,
                NoteUserStats.replied AS userReplied,
                NoteUserStats.reposted AS userReposted,
                NoteUserStats.zapped AS userZapped,
                RepostData.createdAt AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM RepostData
            JOIN PostData ON RepostData.postId = PostData.postId
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = RepostData.repostId
            LEFT JOIN NoteUserStats ON NoteUserStats.postId = PostData.postId AND NoteUserStats.userId = ?
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE FeedPostDataCrossRef.feedDirective = ? AND isMuted = 0
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
