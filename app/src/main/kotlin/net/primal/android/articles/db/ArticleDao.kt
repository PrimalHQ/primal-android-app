package net.primal.android.articles.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(list: List<ArticleData>)

    @Transaction
    @RawQuery(observedEntities = [ArticleData::class])
    fun feed(query: SupportSQLiteQuery): PagingSource<Int, Article>
}
