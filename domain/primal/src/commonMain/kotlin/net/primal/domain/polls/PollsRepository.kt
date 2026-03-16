package net.primal.domain.polls

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface PollsRepository {
    suspend fun fetchPollVotes(eventId: String)

    suspend fun votePoll(
        userId: String,
        pollEventId: String,
        optionId: String,
    ): Result<Unit>

    suspend fun validateZapPollVote(userId: String, pollEventId: String): Result<Unit>

    suspend fun recordZapPollVote(
        userId: String,
        pollEventId: String,
        optionId: String,
        amountInSats: Long,
        zapComment: String?,
    ): Result<Unit>

    fun observePollVotes(eventId: String): Flow<PollVoteStats>

    suspend fun markPollVoted(
        userId: String,
        pollEventId: String,
        optionId: String,
    )

    suspend fun revertPollVoted(userId: String, pollEventId: String)

    fun createVotersPager(eventId: String, optionId: String): Flow<PagingData<PollVoter>>

    fun observePollData(eventId: String, userId: String): Flow<PollInfo?>
}
