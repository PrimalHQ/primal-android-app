package net.primal.domain.polls

import kotlinx.coroutines.flow.Flow

interface PollsRepository {
    suspend fun fetchPollVotes(eventId: String)

    fun observePollVotes(eventId: String): Flow<PollVoteStats>

    fun observeUserVotedOptions(userId: String, postId: String): Flow<Set<String>>
}
