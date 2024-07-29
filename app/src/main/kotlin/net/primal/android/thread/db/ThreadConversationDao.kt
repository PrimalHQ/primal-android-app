package net.primal.android.thread.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.android.feed.db.FeedPost

@Dao
interface ThreadConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connectNoteWithReply(data: List<NoteConversationCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connectArticleWithComment(data: List<ArticleCommentCrossRef>)

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
            INNER JOIN NoteConversationCrossRef ON FPD1.postId = NoteConversationCrossRef.noteId
            INNER JOIN PostData AS FPD2 ON NoteConversationCrossRef.replyNoteId = FPD2.postId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = FPD2.postId AND EventUserStats.userId = :userId
            LEFT JOIN MutedUserData ON MutedUserData.userId = FPD2.authorId
            WHERE FPD1.postId = :postId AND isMuted = 0
            ORDER BY FPD2.createdAt ASC
        """,
    )
    fun observeNoteConversation(postId: String, userId: String): Flow<List<FeedPost>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
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
                EventUserStats.liked AS userLiked,
                EventUserStats.replied AS userReplied,
                EventUserStats.reposted AS userReposted,
                EventUserStats.zapped AS userZapped,
                NULL AS feedCreatedAt, 
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted,
                NULL AS replyToPostId,
                NULL AS replyToAuthorId
            FROM PostData
            INNER JOIN ArticleCommentCrossRef ON PostData.postId = ArticleCommentCrossRef.commentNoteId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = :userId
            LEFT JOIN MutedUserData ON MutedUserData.userId = PostData.authorId
            WHERE ArticleCommentCrossRef.articleId = :articleId 
                AND ArticleCommentCrossRef.articleAuthorId = :articleAuthorId 
                AND isMuted = 0
            ORDER BY PostData.createdAt DESC
        """,
    )
    fun observeArticleComments(
        articleId: String,
        articleAuthorId: String,
        userId: String,
    ): Flow<List<FeedPost>>
}
