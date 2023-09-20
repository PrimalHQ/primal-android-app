package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
@Dao
interface PostStatsDao {

    @Upsert
    fun upsert(data: PostStats)

    @Upsert
    fun upsertAll(data: List<PostStats>)

    @Query("SELECT * FROM PostStats WHERE postId = :postId")
    fun find(postId: String): PostStats?

}
