package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostUserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: PostUserStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<PostUserStats>)

    @Query("SELECT * FROM PostUserStats WHERE postId = :postId AND userId = :userId")
    fun find(postId: String, userId: String): PostUserStats?
}
