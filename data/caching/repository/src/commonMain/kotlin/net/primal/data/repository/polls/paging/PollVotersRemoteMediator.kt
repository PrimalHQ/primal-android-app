package net.primal.data.repository.polls.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import net.primal.core.caching.MediaCacher
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.polls.PollVoteWithProfile
import net.primal.data.local.dao.polls.PollVoterRemoteKey
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.polls.PollsApi
import net.primal.data.remote.api.polls.model.PollVotesRequestBody
import net.primal.data.remote.api.polls.model.PollVotesResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
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
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.shared.data.local.db.withTransaction

@ExperimentalPagingApi
internal class PollVotersRemoteMediator(
    private val postId: String,
    private val optionId: String,
    private val pollsApi: PollsApi,
    private val database: PrimalDatabase,
    private val dispatcherProvider: DispatcherProvider,
    private val mediaCacher: MediaCacher? = null,
) : RemoteMediator<Int, PollVoteWithProfile>() {

    private val lastRequests: MutableMap<LoadType, Pair<PollVotesRequestBody, Long>> =
        mutableMapOf()

    override suspend fun initialize(): InitializeAction {
        val latestRemoteKey = withContext(dispatcherProvider.io()) {
            database.pollVoterRemoteKeys().findLatest(postId = postId, optionId = optionId)
        }

        return latestRemoteKey?.let {
            if (it.cachedAt.isTimestampOlderThan(duration = INITIALIZE_CACHE_EXPIRY)) {
                clearKeysAndVotes()
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        } ?: run {
            clearKeysAndVotes()
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PollVoteWithProfile>): MediatorResult {
        val nextUntil = when (loadType) {
            LoadType.APPEND -> findLastRemoteKey(state = state)?.sinceId
                ?: run {
                    Napier.d("APPEND no remote key found exit.")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.REFRESH -> null
        }

        return try {
            val response = fetchPollVotes(
                pageSize = state.config.pageSize,
                nextUntil = nextUntil,
                loadType = loadType,
            )

            val votesCount = processAndPersistToDatabase(
                response = response,
                clearExisting = loadType == LoadType.REFRESH,
            )

            MediatorResult.Success(endOfPaginationReached = votesCount == 0)
        } catch (error: NetworkException) {
            MediatorResult.Error(error)
        } catch (_: RepeatingRequestBodyException) {
            Napier.d("RepeatingRequestBody exit.")
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun processAndPersistToDatabase(response: PollVotesResponse, clearExisting: Boolean): Int {
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

        val regularVotes = response.pollResponses.mapAsPollResponseVotes()
        val zapVotes = response.zaps.mapAsZapPollVotes()
        val allVotes = regularVotes + zapVotes

        val pollData = response.referencedEvents
            .mapNotNull { it.content.decodeFromJsonStringOrNull<NostrEvent>() }
            .filter { it.kind == NostrEventKind.Poll.value || it.kind == NostrEventKind.ZapPoll.value }
            .mapNotNullAsPollDataPO()
        val statsMap = listOfNotNull(response.pollStats).parseAndMapPrimalPollStats()
        val pollDataWithStats = pollData.filter { it.postId in statsMap }.applyPollStats(statsMap)
        val pollDataWithoutStats = pollData.filter { it.postId !in statsMap }

        val remoteKeys = buildRemoteKeys(allVotes = allVotes, paging = response.paging)

        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                if (clearExisting) {
                    database.pollVoterRemoteKeys().deleteByPostIdAndOptionId(postId, optionId)
                    database.pollVotes().deleteByPostIdAndOptionId(postId, optionId)
                }
                database.profiles().insertOrUpdateAll(data = profiles)
                if (pollDataWithStats.isNotEmpty()) {
                    database.polls().upsertAll(data = pollDataWithStats)
                }
                if (pollDataWithoutStats.isNotEmpty()) {
                    database.polls().insertAllOrIgnore(data = pollDataWithoutStats)
                }
                database.pollVotes().upsertAll(data = allVotes)
                if (remoteKeys.isNotEmpty()) {
                    database.pollVoterRemoteKeys().upsertAll(data = remoteKeys)
                }
            }
        }

        return allVotes.size
    }

    private fun buildRemoteKeys(
        allVotes: List<net.primal.data.local.dao.polls.PollVoteData>,
        paging: net.primal.domain.common.ContentPrimalPaging?,
    ): List<PollVoterRemoteKey> {
        val sinceId = paging?.sinceId
        val untilId = paging?.untilId
        if (sinceId != null && untilId != null) {
            return allVotes.map { vote ->
                PollVoterRemoteKey(
                    postId = postId,
                    optionId = optionId,
                    eventId = vote.eventId,
                    sinceId = sinceId,
                    untilId = untilId,
                    cachedAt = Clock.System.now().epochSeconds,
                )
            }
        }
        return emptyList()
    }

    private suspend fun fetchPollVotes(
        pageSize: Int,
        nextUntil: Long?,
        loadType: LoadType,
    ): PollVotesResponse {
        val request = PollVotesRequestBody(
            eventId = postId,
            option = optionId,
            limit = pageSize,
            until = nextUntil,
        )

        lastRequests[loadType]?.let { (lastRequest, lastRequestAt) ->
            if (request == lastRequest && !lastRequestAt.isRequestCacheExpired() && loadType != LoadType.REFRESH) {
                throw RepeatingRequestBodyException()
            }
        }

        val response = withContext(dispatcherProvider.io()) {
            retryNetworkCall {
                pollsApi.getPollVotes(body = request)
            }
        }

        lastRequests[loadType] = request to Clock.System.now().epochSeconds
        return response
    }

    private suspend fun clearKeysAndVotes() =
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.pollVoterRemoteKeys().deleteByPostIdAndOptionId(postId, optionId)
                database.pollVotes().deleteByPostIdAndOptionId(postId, optionId)
            }
        }

    private suspend fun findLastRemoteKey(state: PagingState<Int, PollVoteWithProfile>): PollVoterRemoteKey? {
        val lastItemEventId = state.lastItemOrNull()?.vote?.eventId

        return withContext(dispatcherProvider.io()) {
            lastItemEventId?.let {
                database.pollVoterRemoteKeys().find(postId = postId, optionId = optionId, eventId = lastItemEventId)
            }
                ?: database.pollVoterRemoteKeys().findLatest(postId = postId, optionId = optionId)
        }
    }

    private fun Long.isTimestampOlderThan(duration: Long) = (Clock.System.now().epochSeconds - this) > duration

    private fun Long.isRequestCacheExpired() = isTimestampOlderThan(duration = LAST_REQUEST_EXPIRY)

    private inner class RepeatingRequestBodyException : RuntimeException()

    companion object {
        private val LAST_REQUEST_EXPIRY = 10.seconds.inWholeSeconds
        private val INITIALIZE_CACHE_EXPIRY = 3.minutes.inWholeSeconds
    }
}
