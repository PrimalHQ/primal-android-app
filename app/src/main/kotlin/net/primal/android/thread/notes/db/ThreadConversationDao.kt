package net.primal.android.thread.notes.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.android.feed.db.FeedPost

@Dao
interface ThreadConversationDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
            SELECT 
                FPD2.postId,
                FPD2.authorId,
                FPD2.createdAt,
                FPD2.content,
                FPD2.raw,
                FPD2.authorMetadataId,
                FPD2.hashtags,
                NULL AS repostId,
                NULL AS repostAuthorId,
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
                NULL AS feedCreatedAt,
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                NULL AS replyToPostId,
                NULL AS replyToAuthorId
            FROM PostData AS FPD1
            INNER JOIN ThreadConversationCrossRef ON FPD1.postId = ThreadConversationCrossRef.postId
            INNER JOIN PostData AS FPD2 ON ThreadConversationCrossRef.replyPostId = FPD2.postId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = FPD2.postId AND EventUserStats.userId = :userId
            LEFT JOIN MutedUserData ON MutedUserData.userId = FPD2.authorId
            WHERE FPD1.postId = :postId AND isMuted = 0
            ORDER BY FPD2.createdAt ASC
        """,
    )
    fun observeConversation(postId: String, userId: String): Flow<List<FeedPost>>
}
