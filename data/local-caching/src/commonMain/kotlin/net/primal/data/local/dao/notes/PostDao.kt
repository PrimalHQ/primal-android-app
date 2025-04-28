package net.primal.data.local.dao.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<PostData>)

    @Query("SELECT * FROM PostData WHERE postId = :postId")
    suspend fun findByPostId(postId: String): PostData?

    @Query("SELECT * FROM PostData WHERE postId IN (:postIds)")
    suspend fun findPosts(postIds: List<String>): List<PostData>

    @Query("DELETE FROM PostData WHERE postId = :postId")
    suspend fun deletePostById(postId: String)

    @Transaction
    suspend fun findAndDeletePostById(postId: String): PostData? =
        findByPostId(postId = postId)?.also { deletePostById(postId = postId) }
}
