package net.primal.domain.polls

interface PollsRepository {
    suspend fun fetchPollVotes(eventId: String): PollVoteStats
}
