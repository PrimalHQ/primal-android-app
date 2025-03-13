package net.primal.db.reads

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}
