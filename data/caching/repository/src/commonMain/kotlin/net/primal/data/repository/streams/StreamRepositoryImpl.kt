package net.primal.data.repository.streams

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.streams.StreamFollowsCrossRef
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.stream.LiveStreamApi
import net.primal.data.remote.api.stream.model.FindLiveStreamRequestBody
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.data.repository.mappers.local.asStreamDO
import net.primal.data.repository.mappers.remote.asChatMessageDataDO
import net.primal.data.repository.mappers.remote.asStreamData
import net.primal.data.repository.mappers.remote.extractZapRequestOrNull
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.Stream
import net.primal.domain.streams.StreamContentModerationMode
import net.primal.domain.streams.StreamRepository
import net.primal.shared.data.local.db.withTransaction

class StreamRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val profileRepository: ProfileRepository,
    private val liveStreamApi: LiveStreamApi,
) : StreamRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io())

    override fun observeLiveStreamsByMainHostId(mainHostId: String) =
        database.streams().observeStreamsByMainHostId(mainHostId = mainHostId)
            .map { list -> list.map { it.asStreamDO() }.filter { it.isLive() } }
            .distinctUntilChanged()

    override suspend fun findWhoIsLive(mainHostIds: List<String>): Set<String> =
        withContext(dispatcherProvider.io()) {
            database.streams()
                .findStreamData(mainHostIds)
                .groupBy { it.mainHostId }
                .mapValues { it.value.any { streamData -> streamData.isLive() } }
                .filter { it.value }
                .keys
        }

    override fun observeStream(aTag: String): Flow<Stream?> {
        return database.streams().observeStreamByATag(aTag = aTag).map { streamPO ->
            streamPO?.asStreamDO()
        }
    }

    override suspend fun getStream(aTag: String): Result<Stream> =
        withContext(dispatcherProvider.io()) {
            database.streams().findStreamByATag(aTag = aTag)?.let {
                Result.success(it.asStreamDO())
            } ?: Result.failure(IllegalArgumentException("stream with given aTag could not be found."))
        }

    override suspend fun getStaleStreamNaddrs(): List<Naddr> =
        withContext(dispatcherProvider.io()) {
            database.streams().findStaleStreamData().map {
                Naddr(
                    kind = NostrEventKind.LiveActivity.value,
                    userId = it.eventAuthorId,
                    identifier = it.dTag,
                )
            }
        }

    override suspend fun awaitLiveStreamSubscriptionStart(
        naddr: Naddr,
        userId: String,
        streamContentModerationMode: StreamContentModerationMode,
    ): Job =
        withContext(dispatcherProvider.io()) {
            val firstEmission = CompletableDeferred<Unit>()

            val job = scope.launch {
                liveStreamApi.subscribeToLiveEvent(
                    streamingNaddr = naddr,
                    userId = userId,
                    contentModerationMode = when (streamContentModerationMode) {
                        StreamContentModerationMode.Moderated -> "moderated"
                        StreamContentModerationMode.None -> "all"
                    },
                ).catch {
                    Napier.w(throwable = it) { "Couldn't subscribe to live feed." }
                    firstEmission.complete(Unit)
                }.collect {
                    processLiveStreamResponse(response = it)
                    firstEmission.complete(Unit)
                }
            }

            firstEmission.await()
            return@withContext job
        }

    override suspend fun startLiveEventsFromFollowsSubscription(userId: String) =
        withContext(dispatcherProvider.io()) {
            database.streamFollows().deleteAllByOwnerId(ownerId = userId)
            liveStreamApi.subscribeToLiveEventsFromFollows(userId = userId)
                .collect { liveActivityEvent ->
                    val liveActivity = liveActivityEvent.asStreamData() ?: return@collect

                    scope.launch {
                        profileRepository.fetchMissingProfiles(profileIds = listOf(liveActivity.mainHostId))
                    }

                    database.withTransaction {
                        database.streams().upsertStreamData(data = listOf(liveActivity))

                        if (liveActivity.isLive()) {
                            database.streamFollows().upsert(
                                data = StreamFollowsCrossRef(
                                    streamATag = liveActivity.aTag,
                                    ownerId = userId,
                                ),
                            )
                        } else {
                            database.streamFollows().deleteByATag(aTag = liveActivity.aTag)
                        }
                    }
                }
        }

    override fun observeLiveEventsFromFollows(userId: String): Flow<List<Stream>> =
        database.streamFollows().observeStreamByOwnerId(ownerId = userId)
            .map { list -> list.map { it.asStreamDO() } }
            .distinctUntilChanged()

    override suspend fun findStreamNaddr(hostPubkey: String, identifier: String): Result<Naddr> =
        withContext(dispatcherProvider.io()) {
            try {
                val response = liveStreamApi.findLiveStream(
                    body = FindLiveStreamRequestBody(hostPubkey = hostPubkey, identifier = identifier),
                )

                val streamData = response.liveActivity?.asStreamData()
                if (streamData == null) {
                    Result.failure(IllegalStateException("Live stream event not found or identifier tag is missing."))
                } else {
                    database.withTransaction {
                        database.streams().upsertStreamData(data = listOf(streamData))
                    }

                    val naddr = Naddr(
                        kind = NostrEventKind.LiveActivity.value,
                        userId = streamData.eventAuthorId,
                        identifier = streamData.dTag,
                    )
                    Result.success(naddr)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun processLiveStreamResponse(response: LiveFeedResponse) {
        val zapEvents = response.zaps
        val zapRequests = zapEvents.mapNotNull { it.extractZapRequestOrNull() }
        val zapperPubkeys = zapRequests.map { it.pubKey }.toSet()

        val chatMessages = response.chatMessages.mapNotNull { it.asChatMessageDataDO() }
        val chatAuthorPubkeys = chatMessages.map { it.authorId }.toSet()

        val allProfileIds = zapperPubkeys + chatAuthorPubkeys
        profileRepository.fetchMissingProfiles(profileIds = allProfileIds.toList())

        val localProfiles = database.profiles().findProfileData(profileIds = allProfileIds.toList())

        val localProfilesMap = localProfiles.associateBy { it.ownerId }
        val eventZaps = zapEvents.mapAsEventZapDO(profilesMap = localProfilesMap)

        database.withTransaction {
            if (eventZaps.isNotEmpty()) {
                database.eventZaps().upsertAll(data = eventZaps)
            }
            if (chatMessages.isNotEmpty()) {
                database.streamChats().upsertAll(data = chatMessages)
            }
        }
    }
}
