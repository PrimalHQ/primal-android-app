package net.primal.android.articles.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArticleFeedCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connect(data: List<ArticleFeedCrossRef>)

    @Query("DELETE FROM ArticleFeedCrossRef WHERE spec = :spec")
    fun deleteConnectionsBySpec(spec: String)
}
