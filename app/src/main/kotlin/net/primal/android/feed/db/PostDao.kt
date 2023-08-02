package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PostDao {

    @Upsert
    fun upsertAll(data: List<PostData>)

    @Query("DELETE FROM PostData WHERE postId NOT IN (SELECT DISTINCT postId FROM FeedPostDataCrossRef)")
    fun deleteOrphanPosts()

    @Query("SELECT * FROM PostData WHERE postId = :postId")
    fun findByPostId(postId: String): PostData?

}
