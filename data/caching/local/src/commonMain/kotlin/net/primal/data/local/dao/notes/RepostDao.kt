package net.primal.data.local.dao.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RepostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<RepostData>)

    @Query("DELETE FROM RepostData WHERE repostId = :repostId")
    suspend fun deleteById(repostId: String)

    @Query("SELECT * FROM RepostData WHERE postId = :postId AND authorId = :authorId LIMIT 1")
    suspend fun findByPostId(postId: String, authorId: String): RepostData?
}
