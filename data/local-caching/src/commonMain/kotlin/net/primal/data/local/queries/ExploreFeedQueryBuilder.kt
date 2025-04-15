package net.primal.data.local.queries

import androidx.room.RoomRawQuery

class ExploreFeedQueryBuilder(
    private val feedSpec: String,
    private val userPubkey: String,
) : FeedQueryBuilder {

    companion object {
        private const val EXPLORE_BASIC_QUERY = """
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
                NULL AS feedCreatedAt,
                CASE WHEN MutedItemData.item IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                PostData.replyToPostId,
                PostData.replyToAuthorId
            FROM PostData
            INNER JOIN FeedPostDataCrossRef ON FeedPostDataCrossRef.eventId = PostData.postId
            INNER JOIN EventStats ON PostData.postId = EventStats.eventId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = ?
            LEFT JOIN MutedItemData ON MutedItemData.item = PostData.authorId
            WHERE FeedPostDataCrossRef.feedSpec = ? AND FeedPostDataCrossRef.ownerId = ? AND isMuted = 0
        """
    }

    private val orderByClause = when {
        else -> "ORDER BY FeedPostDataCrossRef.position"
    }

    override fun feedQuery(): RoomRawQuery {
//        return SimpleSQLiteQuery(
//            query = "$EXPLORE_BASIC_QUERY $orderByClause ASC",
//            bindArgs = arrayOf(userPubkey, feedSpec, userPubkey),
//        )
        return RoomRawQuery(
            sql = "$EXPLORE_BASIC_QUERY $orderByClause ASC",
            onBindStatement = { query ->
                query.bindText(index = 1, userPubkey)
                query.bindText(index = 2, feedSpec)
                query.bindText(index = 3, userPubkey)
            },
        )
    }

    override fun newestFeedPostsQuery(limit: Int): RoomRawQuery {
//        return SimpleSQLiteQuery(
//            query = "$EXPLORE_BASIC_QUERY $orderByClause ASC LIMIT ?",
//            bindArgs = arrayOf(userPubkey, feedSpec, userPubkey, limit),
//        )
        return RoomRawQuery(
            sql = "$EXPLORE_BASIC_QUERY $orderByClause ASC LIMIT ?",
            onBindStatement = { query ->
                query.bindText(index = 1, userPubkey)
                query.bindText(index = 2, feedSpec)
                query.bindText(index = 3, userPubkey)
                query.bindInt(index = 4, limit)
            },
        )
    }

    override fun oldestFeedPostsQuery(limit: Int): RoomRawQuery {
//        return SimpleSQLiteQuery(
//            query = "$EXPLORE_BASIC_QUERY $orderByClause DESC LIMIT ?",
//            bindArgs = arrayOf(userPubkey, feedSpec, userPubkey, limit),
//        )
        return RoomRawQuery(
            sql = "$EXPLORE_BASIC_QUERY $orderByClause DESC LIMIT ?",
            onBindStatement = { query ->
                query.bindText(index = 1, userPubkey)
                query.bindText(index = 2, feedSpec)
                query.bindText(index = 3, userPubkey)
                query.bindInt(index = 4, limit)
            },
        )
    }
}
