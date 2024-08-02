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
            SELECT * 
            FROM ArticleData
            INNER JOIN ArticleFeedCrossRef 
                ON ArticleFeedCrossRef.articleId = ArticleData.articleId 
                AND ArticleFeedCrossRef.articleAuthorId = ArticleData.authorId
            WHERE ArticleFeedCrossRef.spec = :spec
            ORDER BY ArticleData.publishedAt DESC
        """,
    )
    fun feed(spec: String): PagingSource<Int, Article>

    @Transaction
    @Query(
        """
        SELECT * FROM ArticleData WHERE articleId = :articleId AND authorId = :authorId ORDER BY publishedAt DESC LIMIT 1
        """,
    )
    fun observeArticle(articleId: String, authorId: String): Flow<Article>
}
