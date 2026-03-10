package net.primal.data.repository.polls

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import net.primal.core.caching.MediaCacher
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.polls.PollType
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.polls.PollsApi
import net.primal.data.remote.api.polls.model.PollVotesRequestBody
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.remote.applyPollStats
import net.primal.data.repository.mappers.remote.mapAsPollResponseVotes
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.mapAsZapPollVotes
import net.primal.data.repository.mappers.remote.mapNotNullAsPollDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPollStats
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.utils.cacheAvatarUrls
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.polls.PollOptionInfo
import net.primal.domain.polls.PollOptionStats
import net.primal.domain.polls.PollVoteStats
import net.primal.domain.polls.PollVoter
import net.primal.domain.polls.PollsRepository
import net.primal.shared.data.local.db.withTransaction

class PollsRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val pollsApi: PollsApi,
    private val database: PrimalDatabase,
    private val mediaCacher: MediaCacher? = null,
) : PollsRepository {

    override suspend fun fetchPollVotes(eventId: String) =
        withContext(dispatcherProvider.io()) {
            val response = pollsApi.getPollVotes(
                body = PollVotesRequestBody(eventId = eventId, limit = 100),
            )

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

            mediaCacher?.cacheAvatarUrls(metadata = response.profiles, cdnResources = response.cdnResources)
            val profiles = response.profiles.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )

            val pollEvents = response.referencedEvents
                .mapNotNull { it.content.decodeFromJsonStringOrNull<NostrEvent>() }
                .filter { it.kind == NostrEventKind.Poll.value || it.kind == NostrEventKind.ZapPoll.value }

            val pollData = pollEvents.mapNotNullAsPollDataPO()
            val pollVotes = response.pollResponses.mapAsPollResponseVotes() +
                response.zaps.mapAsZapPollVotes()

            val statsMap = response.pollStats.parseAndMapPrimalPollStats()
            val pollDataWithCounts = pollData.applyPollStats(statsMap)

            database.withTransaction {
                database.profiles().insertOrUpdateAll(data = profiles)
                database.polls().upsertAll(data = pollDataWithCounts)
                database.pollVotes().upsertAll(data = pollVotes)
            }
        }

    override fun observeUserVotedOptions(userId: String, postId: String): Flow<Set<String>> {
        return database.pollVotes().observeVotesByUser(postId = postId, voterId = userId)
            .map { votes -> votes.map { it.optionId }.toSet() }
    }

    override fun observePollVotes(eventId: String): Flow<PollVoteStats> {
        return database.polls().observePollByPostId(eventId)
            .mapNotNull { poll ->
                poll ?: return@mapNotNull null

                val voterIds = poll.votes.map { it.voterId }.distinct()
                val profilesMap = if (voterIds.isNotEmpty()) {
                    database.profiles().findProfileData(voterIds).associateBy { it.ownerId }
                } else {
                    emptyMap()
                }

                val votesByOption = poll.votes.groupBy { it.optionId }

                PollVoteStats(
                    eventId = eventId,
                    isZapPoll = poll.data.pollType == PollType.Zap,
                    options = poll.data.options.map { option ->
                        val optionVotes = votesByOption[option.id] ?: emptyList()
                        PollOptionStats(
                            optionInfo = PollOptionInfo(
                                id = option.id,
                                label = option.label,
                                voteCount = option.voteCount,
                                satsZapped = option.satsZapped,
                            ),
                            voters = optionVotes.mapNotNull { vote ->
                                profilesMap[vote.voterId]?.asProfileDataDO()?.let { profile ->
                                    PollVoter(
                                        profile = profile,
                                        satsZapped = vote.amountInSats ?: 0,
                                        zapComment = vote.zapComment,
                                    )
                                }
                            },
                        )
                    },
                )
            }
    }
}
