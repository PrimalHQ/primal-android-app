package net.primal.data.local.dao.reads

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface ArticleFeedCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connect(data: List<ArticleFeedCrossRef>)

    @Query("SELECT * FROM ArticleFeedCrossRef WHERE spec = :spec AND ownerId = :ownerId ORDER BY position DESC LIMIT 1")
    suspend fun findLastBySpec(ownerId: String, spec: String): ArticleFeedCrossRef?

    @Query("DELETE FROM ArticleFeedCrossRef WHERE spec = :spec AND ownerId = :ownerId")
    suspend fun deleteConnectionsBySpec(ownerId: String, spec: String)

    @Query("DELETE FROM ArticleFeedCrossRef WHERE ownerId = :ownerId")
    suspend fun deleteConnections(ownerId: String)

    @Query("DELETE FROM ArticleFeedCrossRef WHERE articleATag = :articleATag")
    suspend fun deleteConnectionsByATag(articleATag: String)
}
