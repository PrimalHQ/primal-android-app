package net.primal.android.thread.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.android.feed.db.FeedPost

@Dao
interface ConversationDao {

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
                PostUserStats.liked AS userLiked,
                PostUserStats.replied AS userReplied,
                PostUserStats.reposted AS userReposted,
                PostUserStats.zapped AS userZapped,
                NULL AS feedCreatedAt
            FROM PostData AS FPD1
            INNER JOIN ConversationCrossRef ON FPD1.postId = ConversationCrossRef.postId
            INNER JOIN PostData AS FPD2 ON ConversationCrossRef.replyPostId = FPD2.postId
            LEFT JOIN PostUserStats ON PostUserStats.postId = FPD2.postId AND PostUserStats.userId = :userId
            WHERE FPD1.postId = :postId
            ORDER BY FPD2.createdAt ASC
        """
    )
    fun observeConversation(postId: String, userId: String): Flow<List<FeedPost>>

}
