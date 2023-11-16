package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: PostStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<PostStats>)

    @Query("SELECT * FROM PostStats WHERE postId = :postId")
    fun find(postId: String): PostStats?
}
