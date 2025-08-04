package net.primal.data.repository.streams

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.profiles.ProfileData as ProfileDataPO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.stream.LiveStreamApi
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.data.remote.api.users.UsersApi
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asStreamDO
import net.primal.data.repository.mappers.remote.extractZapRequestOrNull
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.streams.Stream
import net.primal.domain.streams.StreamRepository

class StreamRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val streamMonitor: LiveStreamApi,
) : StreamRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io())

    override suspend fun findLatestLiveStreamATag(authorId: String): String? {
        val streamsPO = database.streams().observeStreamsByAuthorId(authorId).first()
        val liveStreamPO = streamsPO.find { it.data.isLive() }
        return liveStreamPO?.data?.aTag
    }

    override fun observeStream(aTag: String): Flow<Stream?> {
        return database.streams().observeStreamByATag(aTag = aTag).map { streamPO ->
            streamPO?.asStreamDO()
        }
    }

    override suspend fun startLiveStreamSubscription(naddr: Naddr, userId: String) {
        streamMonitor.subscribe(
            streamingNaddr = naddr,
            userId = userId,
        ).collect { response ->
            processLiveStreamResponse(response = response)
        }
    }

    private suspend fun processLiveStreamResponse(response: LiveFeedResponse) {
        val zapEvents = response.zaps
        if (zapEvents.isEmpty()) return

        val zapRequests = zapEvents.mapNotNull { it.extractZapRequestOrNull() }
        val zapperPubkeys = zapRequests.map { it.pubKey }.toSet()

        if (zapperPubkeys.isNotEmpty()) {
            val localProfiles = withContext(dispatcherProvider.io()) {
                database.profiles().findProfileData(profileIds = zapperPubkeys.toList())
            }
            withContext(dispatcherProvider.io()) {
                saveZaps(zapEvents = zapEvents, profiles = localProfiles)
            }

            val localProfileIds = localProfiles.map { it.ownerId }.toSet()
            val missingProfileIds = zapperPubkeys - localProfileIds
            if (missingProfileIds.isNotEmpty()) {
                scope.launch {
                    fetchAndPersistProfiles(profileIds = missingProfileIds)
                }
            }
        }
    }

    private suspend fun fetchAndPersistProfiles(profileIds: Set<String>) {
        withContext(dispatcherProvider.io()) {
            try {
                val response = usersApi.getUserProfilesMetadata(userIds = profileIds)
                val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
                val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
                val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
                val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
                val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
                val profiles = response.metadataEvents.mapAsProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNames,
                    primalPremiumInfo = primalPremiumInfo,
                    primalLegendProfiles = primalLegendProfiles,
                    blossomServers = blossomServers,
                )
                database.profiles().insertOrUpdateAll(data = profiles)
            } catch (error: NetworkException) {
                Napier.w(error.toString())
            }
        }
    }

    private suspend fun saveZaps(
        zapEvents: List<NostrEvent>,
        profiles: List<ProfileDataPO>,
    ) {
        val profilesMap = profiles.associateBy { it.ownerId }
        val eventZaps = zapEvents.mapAsEventZapDO(profilesMap = profilesMap)

        if (eventZaps.isNotEmpty()) {
            database.eventZaps().upsertAll(data = eventZaps)
        }
    }
}
