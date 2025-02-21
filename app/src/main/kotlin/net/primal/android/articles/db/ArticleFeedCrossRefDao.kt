package net.primal.android.articles.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArticleFeedCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connect(data: List<ArticleFeedCrossRef>)

    @Query("SELECT * FROM ArticleFeedCrossRef WHERE spec = :spec ORDER BY position DESC LIMIT 1")
    fun findLastBySpec(spec: String): ArticleFeedCrossRef?

    @Query("DELETE FROM ArticleFeedCrossRef WHERE spec = :spec")
    fun deleteConnectionsBySpec(spec: String)
}
