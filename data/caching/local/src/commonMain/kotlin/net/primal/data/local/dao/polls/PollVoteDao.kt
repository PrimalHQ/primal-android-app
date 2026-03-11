package net.primal.data.local.dao.polls

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PollVoteDao {

    @Upsert
    suspend fun upsertAll(data: List<PollVoteData>)

    @Query("SELECT * FROM PollVoteData WHERE postId = :postId AND voterId = :voterId")
    fun observeVotesByUser(postId: String, voterId: String): Flow<List<PollVoteData>>

    @Query("SELECT * FROM PollVoteData WHERE postId = :postId AND voterId = :voterId")
    suspend fun findVotesByUser(postId: String, voterId: String): List<PollVoteData>

    @Transaction
    @Query("SELECT * FROM PollVoteData WHERE postId = :postId AND optionId = :optionId ORDER BY createdAt DESC")
    fun pagingSourceByPostIdAndOptionId(postId: String, optionId: String): PagingSource<Int, PollVoteWithProfile>

    @Query("DELETE FROM PollVoteData WHERE postId = :postId AND optionId = :optionId")
    suspend fun deleteByPostIdAndOptionId(postId: String, optionId: String)
}
