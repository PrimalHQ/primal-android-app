package net.primal.data.local.dao.reads

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.data.local.db.ArticleFeedPagingSourceDaoReturnTypeConverter

@Dao
@DaoReturnTypeConverters(ArticleFeedPagingSourceDaoReturnTypeConverter::class)
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<ArticleData>)

    @Transaction
    @Query(
        """
            SELECT
                ArticleData.aTag,
                ArticleData.eventId,
                ArticleData.articleId,
                ArticleData.authorId,
                ArticleData.createdAt,
                ArticleData.title,
                ArticleData.publishedAt,
                ArticleData.imageCdnImage,
                ArticleData.summary,
                ArticleData.wordsCount,
                ArticleData.client,
                CASE WHEN MutedItemData.item IS NOT NULL THEN 1 ELSE 0 END AS isMuted
            FROM ArticleData
            INNER JOIN ArticleFeedCrossRef
                ON ArticleFeedCrossRef.articleATag = ArticleData.aTag
                AND ArticleFeedCrossRef.articleAuthorId = ArticleData.authorId
            LEFT JOIN EventUserStats ON EventUserStats.eventId = ArticleData.eventId AND EventUserStats.userId = :userId
            LEFT JOIN MutedItemData ON MutedItemData.item = ArticleData.authorId
            WHERE ArticleFeedCrossRef.spec = :spec AND ArticleFeedCrossRef.ownerId = :userId AND isMuted = 0
            ORDER BY ArticleFeedCrossRef.position ASC
        """,
    )
    fun feed(spec: String, userId: String): PagingSource<Int, ArticleFeedItem>

    @Transaction
    @Query(
        """
        SELECT * FROM ArticleData 
        WHERE articleId = :articleId AND authorId = :authorId 
        ORDER BY publishedAt DESC LIMIT 1
        """,
    )
    fun observeArticle(articleId: String, authorId: String): Flow<Article?>

    @Transaction
    @Query("SELECT * FROM ArticleData WHERE aTag = :aTag")
    fun observeArticleByATag(aTag: String): Flow<Article?>

    @Transaction
    @Query(
        """
        SELECT * FROM ArticleData
        WHERE eventId = :eventId AND authorId = :authorId
        ORDER BY publishedAt DESC LIMIT 1
        """,
    )
    fun observeArticleByEventId(eventId: String, authorId: String): Flow<Article?>

    @Query("SELECT * FROM ArticleData WHERE aTag = :articleATag")
    suspend fun findArticleByATag(articleATag: String): Article?

    @Query("DELETE FROM ArticleData WHERE aTag = :articleATag")
    suspend fun deleteByATag(articleATag: String)

    @Transaction
    suspend fun findAndDeleteArticleByATag(articleATag: String): Article? =
        findArticleByATag(articleATag = articleATag)?.also { deleteByATag(articleATag = articleATag) }
}
