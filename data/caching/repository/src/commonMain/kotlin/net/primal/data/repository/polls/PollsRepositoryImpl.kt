package net.primal.data.repository.polls

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import net.primal.core.caching.MediaCacher
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.polls.PollData
import net.primal.data.local.dao.polls.PollType
import net.primal.data.local.dao.polls.PollVoteData
import net.primal.data.local.dao.polls.asDO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.polls.PollsApi
import net.primal.data.remote.api.polls.model.PollVotesRequestBody
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asPollInfo
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
import net.primal.data.repository.polls.paging.PollVotersRemoteMediator
import net.primal.data.repository.utils.cacheAvatarUrls
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asResponseTag
import net.primal.domain.polls.PollAlreadyVotedException
import net.primal.domain.polls.PollAuthorCannotVoteException
import net.primal.domain.polls.PollExpiredException
import net.primal.domain.polls.PollInfo
import net.primal.domain.polls.PollOptionInfo
import net.primal.domain.polls.PollOptionStats
import net.primal.domain.polls.PollVoteStats
import net.primal.domain.polls.PollVoter
import net.primal.domain.polls.PollsRepository
import net.primal.domain.profile.ProfileData
import net.primal.domain.publisher.PrimalPublisher
import net.primal.shared.data.local.db.withTransaction

class PollsRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val pollsApi: PollsApi,
    private val primalPublisher: PrimalPublisher,
    private val database: PrimalDatabase,
    private val mediaCacher: MediaCacher? = null,
) : PollsRepository {

    companion object {
        private const val VOTERS_PAGE_SIZE = 20
    }

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
            val isZapPoll = pollData.any { it.pollType == PollType.Zap }
            val pollVotes = if (isZapPoll) {
                response.zaps.mapAsZapPollVotes()
            } else {
                response.pollResponses.mapAsPollResponseVotes()
            }

            val statsMap = response.pollStats.parseAndMapPrimalPollStats()
            val pollDataWithCounts = pollData.applyPollStats(statsMap)

            database.withTransaction {
                database.profiles().insertOrUpdateAll(data = profiles)
                database.polls().upsertAll(data = pollDataWithCounts)
                database.pollVotes().upsertAll(data = pollVotes)
            }
        }

    private suspend fun fetchPollOrThrow(pollEventId: String): PollData {
        return database.polls().findByPostId(postId = pollEventId)
            ?: error("Poll not found: $pollEventId")
    }

    private fun validateNotExpired(poll: PollData) {
        val endsAt = poll.endsAt ?: return
        if (endsAt <= Clock.System.now().epochSeconds) throw PollExpiredException()
    }

    private fun validateNotOwnPoll(userId: String, poll: PollData) {
        if (poll.authorId == userId) throw PollAuthorCannotVoteException()
    }

    private suspend fun validateNotAlreadyVoted(userId: String, pollEventId: String) {
        val existingVotes = database.pollVotes().findVotesByUser(postId = pollEventId, voterId = userId)
        if (existingVotes.isNotEmpty()) throw PollAlreadyVotedException()

        val userStats = database.eventUserStats().find(eventId = pollEventId, userId = userId)
        if (userStats?.votedForOption != null) throw PollAlreadyVotedException()
    }

    private suspend fun markVotedInUserStats(
        userId: String,
        eventId: String,
        optionId: String,
    ) {
        val existing = database.eventUserStats().find(eventId = eventId, userId = userId)
            ?: EventUserStats(eventId = eventId, userId = userId)
        database.eventUserStats().upsert(existing.copy(votedForOption = optionId))
    }

    private suspend fun revertVotedInUserStats(userId: String, eventId: String) {
        val existing = database.eventUserStats().find(eventId = eventId, userId = userId)
            ?: return
        database.eventUserStats().upsert(existing.copy(votedForOption = null))
    }

    override suspend fun markPollVoted(
        userId: String,
        pollEventId: String,
        optionId: String,
    ) = withContext(dispatcherProvider.io()) {
        markVotedInUserStats(userId = userId, eventId = pollEventId, optionId = optionId)
    }

    override suspend fun revertPollVoted(userId: String, pollEventId: String) =
        withContext(dispatcherProvider.io()) {
            revertVotedInUserStats(userId = userId, eventId = pollEventId)
        }

    override suspend fun votePoll(
        userId: String,
        pollEventId: String,
        optionId: String,
    ): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val poll = fetchPollOrThrow(pollEventId)
                validateNotExpired(poll)
                validateNotAlreadyVoted(userId, pollEventId)

                markVotedInUserStats(userId = userId, eventId = pollEventId, optionId = optionId)

                runCatching {
                    val publishResult = primalPublisher.signPublishImportNostrEvent(
                        unsignedNostrEvent = NostrUnsignedEvent(
                            pubKey = userId,
                            kind = NostrEventKind.PollResponse.value,
                            tags = listOf(pollEventId.asEventIdTag(), optionId.asResponseTag()),
                            content = "",
                        ),
                    )

                    database.withTransaction {
                        database.pollVotes().upsertAll(
                            data = listOf(
                                PollVoteData(
                                    eventId = publishResult.nostrEvent.id,
                                    postId = pollEventId,
                                    optionId = optionId,
                                    voterId = userId,
                                    amountInSats = null,
                                    createdAt = publishResult.nostrEvent.createdAt,
                                ),
                            ),
                        )

                        val existingPoll = database.polls().findByPostId(postId = pollEventId)
                        if (existingPoll != null) {
                            val updatedOptions = existingPoll.options.map { option ->
                                if (option.id == optionId) {
                                    option.copy(voteCount = option.voteCount + 1)
                                } else {
                                    option
                                }
                            }
                            database.polls().upsertAll(listOf(existingPoll.copy(options = updatedOptions)))
                        }
                    }
                }.onFailure {
                    revertVotedInUserStats(userId = userId, eventId = pollEventId)
                }.getOrThrow()
            }
        }

    override suspend fun validateZapPollVote(userId: String, pollEventId: String): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val poll = fetchPollOrThrow(pollEventId)
                validateNotExpired(poll)
                validateNotOwnPoll(userId, poll)
                validateNotAlreadyVoted(userId, pollEventId)
            }
        }

    override suspend fun recordZapPollVote(
        userId: String,
        pollEventId: String,
        optionId: String,
        amountInSats: Long,
        zapComment: String?,
    ): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                database.withTransaction {
                    database.pollVotes().upsertAll(
                        data = listOf(
                            PollVoteData(
                                eventId = "${pollEventId}_${userId}_zap",
                                postId = pollEventId,
                                optionId = optionId,
                                voterId = userId,
                                amountInSats = amountInSats,
                                zapComment = zapComment,
                                createdAt = Clock.System.now().epochSeconds,
                            ),
                        ),
                    )

                    val existingPoll = database.polls().findByPostId(postId = pollEventId)
                    if (existingPoll != null) {
                        val updatedOptions = existingPoll.options.map { option ->
                            if (option.id == optionId) {
                                option.copy(
                                    voteCount = option.voteCount + 1,
                                    satsZapped = option.satsZapped + amountInSats,
                                )
                            } else {
                                option
                            }
                        }
                        database.polls().upsertAll(listOf(existingPoll.copy(options = updatedOptions)))
                    }
                }
            }
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
                    pollType = poll.data.pollType.asDO(),
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
                                        eventId = vote.eventId,
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

    @OptIn(ExperimentalPagingApi::class)
    override fun createVotersPager(eventId: String, optionId: String): Flow<PagingData<PollVoter>> =
        Pager(
            config = PagingConfig(
                pageSize = VOTERS_PAGE_SIZE,
                prefetchDistance = VOTERS_PAGE_SIZE * 2,
                initialLoadSize = VOTERS_PAGE_SIZE * 2,
                enablePlaceholders = false,
            ),
            remoteMediator = PollVotersRemoteMediator(
                postId = eventId,
                optionId = optionId,
                pollsApi = pollsApi,
                database = database,
                dispatcherProvider = dispatcherProvider,
                mediaCacher = mediaCacher,
            ),
            pagingSourceFactory = {
                database.pollVotes().pagingSourceByPostIdAndOptionId(
                    postId = eventId,
                    optionId = optionId,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { voteWithProfile ->
                PollVoter(
                    eventId = voteWithProfile.vote.eventId,
                    profile = voteWithProfile.profile?.asProfileDataDO()
                        ?: ProfileData(profileId = voteWithProfile.vote.voterId),
                    satsZapped = voteWithProfile.vote.amountInSats ?: 0L,
                    zapComment = voteWithProfile.vote.zapComment,
                )
            }
        }

    override fun observePollData(eventId: String, userId: String): Flow<PollInfo?> =
        database.polls().observePollDataByPostIdAndUserId(postId = eventId, userId = userId)
            .distinctUntilChanged()
            .map { it?.asPollInfo() }
}
