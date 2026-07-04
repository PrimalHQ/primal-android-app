package net.primal.data.local.dao.notes

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface RepostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<RepostData>)

    @Query("DELETE FROM RepostData WHERE repostId = :repostId")
    suspend fun deleteById(repostId: String)

    @Query("SELECT * FROM RepostData WHERE postId = :postId AND authorId = :authorId LIMIT 1")
    suspend fun findByPostId(postId: String, authorId: String): RepostData?
}
