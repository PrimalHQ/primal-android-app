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
            SELECT FPD2.*
            FROM FeedPostData AS FPD1
            INNER JOIN ConversationCrossRef ON FPD1.postId = ConversationCrossRef.postId
            INNER JOIN FeedPostData AS FPD2 ON ConversationCrossRef.replyPostId = FPD2.postId
            WHERE FPD1.postId = :postId
            ORDER BY FPD2.feedCreatedAt ASC
        """
    )
    fun observeConversation(postId: String): Flow<List<FeedPost>>

}
