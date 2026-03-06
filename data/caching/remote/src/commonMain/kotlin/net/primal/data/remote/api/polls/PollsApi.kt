package net.primal.data.remote.api.polls

import net.primal.data.remote.api.polls.model.PollVotesRequestBody
import net.primal.data.remote.api.polls.model.PollVotesResponse

interface PollsApi {
    suspend fun getPollVotes(body: PollVotesRequestBody): PollVotesResponse
}
