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
                CASE WHEN MutedUser.item IS NOT NULL THEN 1 ELSE 0 END AS isAuthorMuted,
                CASE WHEN MutedThread.item IS NOT NULL THEN 1 ELSE 0 END AS isThreadMuted,
                FeedPostDataCrossRef.position AS position,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM PostData
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = ?
            LEFT JOIN MutedItemData AS MutedUser ON MutedUser.item = PostData.authorId AND MutedUser.ownerId = ?
            LEFT JOIN MutedItemData AS MutedThread ON MutedThread.item = PostData.postId AND MutedThread.ownerId = ?
            WHERE FeedPostDataCrossRef.feedSpec = ? AND FeedPostDataCrossRef.ownerId = ? 
                AND isAuthorMuted = 0 AND isThreadMuted = 0

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
                CASE WHEN MutedUser.item IS NOT NULL THEN 1 ELSE 0 END AS isAuthorMuted,
                CASE WHEN MutedThread.item IS NOT NULL THEN 1 ELSE 0 END AS isThreadMuted,
                FeedPostDataCrossRef.position AS position,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM RepostData
            JOIN PostData ON RepostData.postId = PostData.postId
            JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = RepostData.repostId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = ?
            LEFT JOIN MutedItemData AS MutedUser ON MutedUser.item = PostData.authorId AND MutedUser.ownerId = ?
            LEFT JOIN MutedItemData AS MutedThread ON MutedThread.item = PostData.postId AND MutedThread.ownerId = ?
            WHERE FeedPostDataCrossRef.feedSpec = ? AND FeedPostDataCrossRef.ownerId = ? 
                AND isAuthorMuted = 0 AND isThreadMuted = 0
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
                query.bindText(index = 2, value = userPubkey)
                query.bindText(index = 3, value = userPubkey)
                query.bindText(index = 4, value = feedSpec)
                query.bindText(index = 5, value = userPubkey)
                query.bindText(index = 6, value = userPubkey)
                query.bindText(index = 7, value = userPubkey)
                query.bindText(index = 8, value = userPubkey)
                query.bindText(index = 9, value = feedSpec)
                query.bindText(index = 10, value = userPubkey)
            },
        )
    }

    override fun newestFeedPostsQuery(limit: Int): RoomRawQuery {
        return RoomRawQuery(
            sql = "$LATEST_BASIC_QUERY $orderByClause ASC LIMIT ?",
            onBindStatement = { query ->
                query.bindText(index = 1, value = userPubkey)
                query.bindText(index = 2, value = userPubkey)
                query.bindText(index = 3, value = userPubkey)
                query.bindText(index = 4, value = feedSpec)
                query.bindText(index = 5, value = userPubkey)
                query.bindText(index = 6, value = userPubkey)
                query.bindText(index = 7, value = userPubkey)
                query.bindText(index = 8, value = userPubkey)
                query.bindText(index = 9, value = feedSpec)
                query.bindText(index = 10, value = userPubkey)
                query.bindInt(index = 11, value = limit)
            },
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): RoomRawQuery {
        return RoomRawQuery(
            sql = "$LATEST_BASIC_QUERY $orderByClause DESC LIMIT ?",
            onBindStatement = { query ->
                query.bindText(index = 1, value = userPubkey)
                query.bindText(index = 2, value = userPubkey)
                query.bindText(index = 3, value = userPubkey)
                query.bindText(index = 4, value = feedSpec)
                query.bindText(index = 5, value = userPubkey)
                query.bindText(index = 6, value = userPubkey)
                query.bindText(index = 7, value = userPubkey)
                query.bindText(index = 8, value = userPubkey)
                query.bindText(index = 9, value = feedSpec)
                query.bindText(index = 10, value = userPubkey)
                query.bindInt(index = 11, value = limit)
            },
        )
    }
}
