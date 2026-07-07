package net.primal.data.local.dao.polls

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
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
