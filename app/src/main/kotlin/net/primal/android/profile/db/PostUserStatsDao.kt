package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PostUserStatsDao {

    @Upsert
    fun upsert(data: PostUserStats)

    @Upsert
    fun upsertAll(data: List<PostUserStats>)

    @Query("SELECT * FROM PostUserStats WHERE postId = :postId AND userId = :userId")
    fun find(postId: String, userId: String): PostUserStats?
}
