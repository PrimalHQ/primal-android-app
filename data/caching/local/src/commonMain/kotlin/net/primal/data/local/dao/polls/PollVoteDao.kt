package net.primal.data.local.dao.polls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PollVoteDao {

    @Upsert
    suspend fun upsertAll(data: List<PollVoteData>)

    @Query("SELECT * FROM PollVoteData WHERE postId = :postId AND voterId = :voterId")
    fun observeVotesByUser(postId: String, voterId: String): Flow<List<PollVoteData>>
}
