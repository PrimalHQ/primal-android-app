package net.primal.data.local.dao.polls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {

    @Upsert
    suspend fun upsertAll(data: List<PollData>)

    @Transaction
    @Query("SELECT * FROM PollData WHERE postId = :postId")
    fun observePollByPostId(postId: String): Flow<Poll?>
}
