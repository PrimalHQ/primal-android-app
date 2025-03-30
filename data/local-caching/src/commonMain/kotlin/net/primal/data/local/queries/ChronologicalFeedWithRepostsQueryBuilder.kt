package net.primal.data.local.queries

import androidx.room.RoomRawQuery

class ChronologicalFeedWithRepostsQueryBuilder(
    private val feedSpec: String,
    private val userPubkey: String,
) : FeedQueryBuilder {

    companion object {
        private const val LATEST_BASIC_QUERY = """
            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.tags,
                PostData.raw,
                PostData.authorMetadataId,
                PostData.hashtags,
                NULL AS repostId,
                NULL AS repostAuthorId,
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
                PostData.createdAt AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                FeedPostDataCrossRef.position AS position,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM PostData
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = ?
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE FeedPostDataCrossRef.feedSpec = ? AND FeedPostDataCrossRef.ownerId = ? AND isMuted = 0

            UNION ALL

            SELECT
                PostData.postId,
                PostData.authorId,
                PostData.createdAt,
                PostData.content,
                PostData.tags,
                PostData.raw,
                PostData.authorMetadataId,
                PostData.hashtags,
                RepostData.repostId AS repostId,
                RepostData.authorId AS repostAuthorId,
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
                RepostData.createdAt AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                FeedPostDataCrossRef.position AS position,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM RepostData
            JOIN PostData ON RepostData.postId = PostData.postId
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = RepostData.repostId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = ?
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE FeedPostDataCrossRef.feedSpec = ? AND FeedPostDataCrossRef.ownerId = ? AND isMuted = 0
        """
    }

    private val orderByClause = when {
        else -> "ORDER BY position"
    }

    override fun feedQuery(): RoomRawQuery {
        return RoomRawQuery(
            sql = "$LATEST_BASIC_QUERY $orderByClause ASC",
            onBindStatement = { query ->
                query.bindText(index = 1, value = userPubkey)
                query.bindText(index = 2, value = feedSpec)
                query.bindText(index = 3, value = userPubkey)
                query.bindText(index = 4, value = userPubkey)
                query.bindText(index = 5, value = feedSpec)
                query.bindText(index = 6, value = userPubkey)
            },
        )
    }

    override fun newestFeedPostsQuery(limit: Int): RoomRawQuery {
        return RoomRawQuery(
            sql = "$LATEST_BASIC_QUERY $orderByClause ASC LIMIT ?",
            onBindStatement = { query ->
                query.bindText(index = 1, value = userPubkey)
                query.bindText(index = 2, value = feedSpec)
                query.bindText(index = 3, value = userPubkey)
                query.bindText(index = 4, value = userPubkey)
                query.bindText(index = 5, value = feedSpec)
                query.bindText(index = 6, value = userPubkey)
                query.bindInt(index = 7, value = limit)
            },
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): RoomRawQuery {
        return RoomRawQuery(
            sql = "$LATEST_BASIC_QUERY $orderByClause DESC LIMIT ?",
            onBindStatement = { query ->
                query.bindText(index = 1, value = userPubkey)
                query.bindText(index = 2, value = feedSpec)
                query.bindText(index = 3, value = userPubkey)
                query.bindText(index = 4, value = userPubkey)
                query.bindText(index = 5, value = feedSpec)
                query.bindText(index = 6, value = userPubkey)
                query.bindInt(index = 7, value = limit)
            },
        )
    }
}
