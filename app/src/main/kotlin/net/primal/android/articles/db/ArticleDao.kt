package net.primal.android.articles.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(list: List<ArticleData>)

    @Transaction
    @RawQuery(observedEntities = [ArticleData::class])
    fun feed(query: SupportSQLiteQuery): PagingSource<Int, Article>

    @Transaction
    @Query(
        """
        SELECT * FROM ArticleData WHERE articleId = :articleId AND authorId = :userId ORDER BY publishedAt DESC LIMIT 1
        """,
    )
    fun observeArticle(articleId: String, userId: String): Flow<Article>
}
