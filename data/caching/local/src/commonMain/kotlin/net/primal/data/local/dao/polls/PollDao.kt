package net.primal.data.local.dao.polls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PollDao {

    @Upsert
    suspend fun upsertAll(data: List<PollData>)

    @Query("SELECT * FROM PollData WHERE postId = :postId")
    suspend fun findByPostId(postId: String): PollData?
}
