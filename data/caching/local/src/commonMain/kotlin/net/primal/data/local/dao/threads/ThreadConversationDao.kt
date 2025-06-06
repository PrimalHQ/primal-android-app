package net.primal.data.local.dao.threads

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.data.local.dao.notes.FeedPost

@Dao
interface ThreadConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connectNoteWithReply(data: List<NoteConversationCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connectArticleWithComment(data: List<ArticleCommentCrossRef>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
            SELECT 
                FPD2.postId,
                FPD2.authorId,
                FPD2.createdAt,
                FPD2.content,
                FPD2.tags,
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
                CASE WHEN MutedUser.item IS NOT NULL THEN 1 ELSE 0 END AS isAuthorMuted,
                CASE WHEN MutedThread.item IS NOT NULL THEN 1 ELSE 0 END AS isThreadMuted,
                NULL AS replyToPostId,
                NULL AS replyToAuthorId
            FROM PostData AS FPD1
            INNER JOIN NoteConversationCrossRef ON FPD1.postId = NoteConversationCrossRef.noteId
            INNER JOIN PostData AS FPD2 ON NoteConversationCrossRef.replyNoteId = FPD2.postId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = FPD2.postId AND EventUserStats.userId = :userId
            LEFT JOIN MutedItemData AS MutedUser ON MutedUser.item = FPD2.authorId AND MutedUser.ownerId = :userId
            LEFT JOIN MutedItemData AS MutedThread ON MutedThread.item = FPD2.postId AND MutedThread.ownerId = :userId 
            WHERE FPD1.postId = :postId AND isAuthorMuted = 0 AND isThreadMuted = 0
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
                CASE WHEN MutedUser.item IS NOT NULL THEN 1 ELSE 0 END AS isAuthorMuted,
                CASE WHEN MutedThread.item IS NOT NULL THEN 1 ELSE 0 END AS isThreadMuted,
                NULL AS replyToPostId,
                NULL AS replyToAuthorId
            FROM PostData
            INNER JOIN ArticleCommentCrossRef ON PostData.postId = ArticleCommentCrossRef.commentNoteId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = PostData.postId AND EventUserStats.userId = :userId
            LEFT JOIN MutedItemData AS MutedUser ON MutedUser.item = PostData.authorId AND MutedUser.ownerId = :userId
            LEFT JOIN MutedItemData AS MutedThread ON MutedThread.item = PostData.postId AND MutedThread.ownerId = :userId
            WHERE ArticleCommentCrossRef.articleId = :articleId
                AND ArticleCommentCrossRef.articleAuthorId = :articleAuthorId
                AND isAuthorMuted = 0 AND isThreadMuted = 0
            ORDER BY PostData.createdAt DESC
        """,
    )
    fun observeArticleComments(
        articleId: String,
        articleAuthorId: String,
        userId: String,
    ): Flow<List<FeedPost>>

    @Query("SELECT * FROM ArticleCommentCrossRef WHERE commentNoteId = :commentNoteId")
    suspend fun findCrossRefByCommentId(commentNoteId: String): ArticleCommentCrossRef?

    @Query("DELETE FROM ArticleCommentCrossRef WHERE articleId = :articleId AND articleAuthorId = :articleAuthorId")
    suspend fun deleteArticleCrossRefs(articleId: String, articleAuthorId: String)
}
