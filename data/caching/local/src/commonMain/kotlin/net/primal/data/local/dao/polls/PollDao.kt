package net.primal.data.local.dao.polls

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {

    @Upsert
    suspend fun upsertAll(data: List<PollData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllOrIgnore(data: List<PollData>)

    @Query("SELECT * FROM PollData WHERE postId = :postId")
    suspend fun findByPostId(postId: String): PollData?

    @Transaction
    @Query("SELECT * FROM PollData WHERE postId = :postId")
    fun observePollByPostId(postId: String): Flow<Poll?>

    @Query("SELECT * FROM PollData WHERE postId = :postId")
    fun observePollDataByPostId(postId: String): Flow<PollData?>

    @Query(
        """
        SELECT PollData.*, EventUserStats.votedForOption AS userVotedForOption
        FROM PollData
        LEFT JOIN EventUserStats ON EventUserStats.eventId = PollData.postId AND EventUserStats.userId = :userId
        WHERE PollData.postId = :postId
        """,
    )
    fun observePollDataByPostIdAndUserId(postId: String, userId: String): Flow<PollDataWithUserVote?>
}
