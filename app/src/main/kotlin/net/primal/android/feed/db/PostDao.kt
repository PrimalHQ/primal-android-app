package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<PostData>)

    @Query("DELETE FROM PostData WHERE postId NOT IN (SELECT DISTINCT postId FROM FeedPostDataCrossRef)")
    fun deleteOrphanPosts()

    @Query("SELECT * FROM PostData WHERE postId = :postId")
    fun findByPostId(postId: String): PostData?

    @Query("SELECT * FROM PostData WHERE postId IN (:postIds)")
    fun findPosts(postIds: List<String>): List<PostData>
}
