package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

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
                FPD2.referencePostId,
                FPD2.referencePostAuthorId,
                NULL AS repostId,
                NULL AS repostAuthorId,
                NULL AS feedCreatedAt
            FROM PostData AS FPD1
            INNER JOIN ConversationCrossRef ON FPD1.postId = ConversationCrossRef.postId
            INNER JOIN PostData AS FPD2 ON ConversationCrossRef.replyPostId = FPD2.postId
            WHERE FPD1.postId = :postId
            ORDER BY FPD2.createdAt ASC
        """
    )
    fun observeConversation(postId: String): Flow<List<FeedPost>>

}
