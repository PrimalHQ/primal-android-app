package net.primal.data.repository.streams

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.stream.LiveStreamApi
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.data.repository.mappers.local.asStreamDO
import net.primal.data.repository.mappers.remote.asChatMessageDataDO
import net.primal.data.repository.mappers.remote.extractZapRequestOrNull
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.domain.nostr.Naddr
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

    override suspend fun findLatestLiveStreamATag(mainHostId: String): String? =
        withContext(dispatcherProvider.io()) {
            val streamsPO = database.streams().observeStreamsByAuthorId(mainHostId).first()
            val liveStreamPO = streamsPO.find { it.data.isLive() }
            liveStreamPO?.data?.aTag
        }

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

    override suspend fun startLiveStreamSubscription(
        naddr: Naddr,
        userId: String,
        streamContentModerationMode: StreamContentModerationMode,
    ) = withContext(dispatcherProvider.io()) {
        liveStreamApi.subscribe(
            streamingNaddr = naddr,
            userId = userId,
            contentModerationMode = when (streamContentModerationMode) {
                StreamContentModerationMode.Moderated -> "moderated"
                StreamContentModerationMode.None -> "all"
            },
        ).collect { response ->
            processLiveStreamResponse(response = response)
        }
    }

    private suspend fun processLiveStreamResponse(response: LiveFeedResponse) {
        val zapEvents = response.zaps
        val zapRequests = zapEvents.mapNotNull { it.extractZapRequestOrNull() }
        val zapperPubkeys = zapRequests.map { it.pubKey }.toSet()

        val chatMessages = response.chatMessages.mapNotNull { it.asChatMessageDataDO() }
        val chatAuthorPubkeys = chatMessages.map { it.authorId }.toSet()

        val allProfileIds = zapperPubkeys + chatAuthorPubkeys
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

        val missingProfileIds = allProfileIds - localProfiles.map { it.ownerId }.toSet()
        if (missingProfileIds.isNotEmpty()) {
            scope.launch {
                try {
                    profileRepository.fetchProfiles(profileIds = missingProfileIds.toList())
                } catch (error: Exception) {
                    Napier.w(error) { "Failed to fetch profiles for stream zaps/chats." }
                }
            }
        }
    }
}
