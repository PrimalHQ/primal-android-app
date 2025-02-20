package net.primal.android.articles.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(list: List<ArticleData>)

    @Transaction
    @Query(
        """
            SELECT 
                ArticleData.*, 
                CASE WHEN MutedUserData.userId IS NOT NULL THEN 1 ELSE 0 END AS isMuted
            FROM ArticleData
            INNER JOIN ArticleFeedCrossRef 
                ON ArticleFeedCrossRef.articleId = ArticleData.articleId 
                AND ArticleFeedCrossRef.articleAuthorId = ArticleData.authorId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = ArticleData.eventId AND EventUserStats.userId = :userId
            LEFT JOIN MutedUserData ON MutedUserData.userId = ArticleData.authorId
            WHERE ArticleFeedCrossRef.spec = :spec AND isMuted = 0
            ORDER BY ArticleFeedCrossRef.position ASC
        """,
    )
    fun feed(spec: String, userId: String): PagingSource<Int, Article>

    @Transaction
    @Query(
        """
        SELECT * FROM ArticleData 
        WHERE articleId = :articleId AND authorId = :authorId 
        ORDER BY publishedAt DESC LIMIT 1
        """,
    )
    fun observeArticle(articleId: String, authorId: String): Flow<Article>

    @Transaction
    @Query(
        """
        SELECT * FROM ArticleData
        WHERE eventId = :eventId AND authorId = :authorId
        ORDER BY publishedAt DESC LIMIT 1
        """,
    )
    fun observeArticleByEventId(eventId: String, authorId: String): Flow<Article>
}
