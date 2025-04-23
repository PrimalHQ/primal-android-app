package net.primal.data.local.queries

import androidx.room.RoomRawQuery

class ChronologicalFeedWithRepostsQueryBuilder(
    private val feedSpec: String,
    private val userPubkey: String,
    private val allowMutedThreads: Boolean,
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
                NULL AS repostCreatedAt,
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
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
                AND isAuthorMuted = 0 AND (isThreadMuted = 0 OR ?)

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
                RepostData.createdAt AS repostCreatedAt,
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
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
                AND isAuthorMuted = 0 AND (isThreadMuted = 0 OR ?)
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
                query.bindBoolean(index = 6, value = allowMutedThreads)
                query.bindText(index = 7, value = userPubkey)
                query.bindText(index = 8, value = userPubkey)
                query.bindText(index = 9, value = userPubkey)
                query.bindText(index = 10, value = feedSpec)
                query.bindText(index = 11, value = userPubkey)
                query.bindBoolean(index = 12, value = allowMutedThreads)
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
                query.bindBoolean(index = 6, value = allowMutedThreads)
                query.bindText(index = 7, value = userPubkey)
                query.bindText(index = 8, value = userPubkey)
                query.bindText(index = 9, value = userPubkey)
                query.bindText(index = 10, value = feedSpec)
                query.bindText(index = 11, value = userPubkey)
                query.bindBoolean(index = 12, value = allowMutedThreads)
                query.bindInt(index = 13, value = limit)
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
                query.bindBoolean(index = 6, value = allowMutedThreads)
                query.bindText(index = 7, value = userPubkey)
                query.bindText(index = 8, value = userPubkey)
                query.bindText(index = 9, value = userPubkey)
                query.bindText(index = 10, value = feedSpec)
                query.bindText(index = 11, value = userPubkey)
                query.bindBoolean(index = 12, value = allowMutedThreads)
                query.bindInt(index = 13, value = limit)
            },
        )
    }
}
