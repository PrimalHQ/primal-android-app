package net.primal.data.local.dao.polls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PollVoterRemoteKeyDao {

    @Upsert
    suspend fun upsertAll(data: List<PollVoterRemoteKey>)

    @Query(
        """
            SELECT * FROM PollVoterRemoteKey
            WHERE postId = :postId AND optionId = :optionId AND eventId = :eventId
            """,
    )
    suspend fun find(
        postId: String,
        optionId: String,
        eventId: String,
    ): PollVoterRemoteKey?

    @Query(
        """
            SELECT * FROM PollVoterRemoteKey
            WHERE postId = :postId AND optionId = :optionId
            ORDER BY cachedAt DESC LIMIT 1
        """,
    )
    suspend fun findLatest(postId: String, optionId: String): PollVoterRemoteKey?

    @Query("DELETE FROM PollVoterRemoteKey WHERE postId = :postId AND optionId = :optionId")
    suspend fun deleteByPostIdAndOptionId(postId: String, optionId: String)
}
