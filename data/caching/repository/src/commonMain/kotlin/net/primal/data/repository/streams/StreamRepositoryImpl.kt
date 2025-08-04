package net.primal.data.repository.streams

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.stream.StreamMonitor
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
    private val streamMonitor: StreamMonitor,
) : StreamRepository {

    override suspend fun findLatestLiveStreamATag(authorId: String): String? {
        val streamsPO = database.streams().observeStreamsByAuthorId(authorId).first()
        val liveStreamPO = streamsPO.find { it.data.isLive() }
        return liveStreamPO?.data?.aTag
    }

    override fun observeStream(aTag: String): Flow<Stream?> {
        return database.streams().observeStreamByATag(aTag = aTag)
            .map { streamPO ->
                streamPO?.asStreamDO()
            }
    }

    override fun startMonitoring(
        scope: CoroutineScope,
        naddr: Naddr,
        userId: String,
    ) {
        streamMonitor.start(
            scope = scope,
            creatorPubkey = naddr.userId,
            dTag = naddr.identifier,
            userPubkey = userId,
        ) { zapEvent ->
            scope.launch {
                processIncomingZap(zapEvent = zapEvent)
            }
        }
    }

    override fun stopMonitoring(scope: CoroutineScope) {
        streamMonitor.stop(scope)
    }

    private suspend fun processIncomingZap(zapEvent: NostrEvent) =
        withContext(dispatcherProvider.io()) {
            try {
                val zapRequest = zapEvent.extractZapRequestOrNull()
                val zapperPubkey = zapRequest?.pubKey
                if (zapperPubkey == null) {
                    Napier.w("Unable to extract zapper pubkey from zap event.")
                    return@withContext
                }

                val localProfile = database.profiles().findProfileData(profileId = zapperPubkey)
                if (localProfile == null) {
                    try {
                        val response = usersApi.getUserProfilesMetadata(userIds = setOf(zapperPubkey))
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

                saveZap(zapEvent = zapEvent)
            } catch (error: NetworkException) {
                Napier.w(error.toString())
            }
        }

    private suspend fun saveZap(zapEvent: NostrEvent) {
        val zapRequest = zapEvent.extractZapRequestOrNull() ?: run {
            Napier.w("Could not extract zap request from zap event ${zapEvent.id}")
            return
        }

        val zapperPubkey = zapRequest.pubKey
        val zapperProfile = database.profiles().findProfileData(profileId = zapperPubkey)

        val profiles = if (zapperProfile != null) listOf(zapperProfile) else emptyList()
        val profilesMap = profiles.associateBy { it.ownerId }

        val eventZaps = listOf(zapEvent).mapAsEventZapDO(profilesMap = profilesMap)

        if (eventZaps.isNotEmpty()) {
            database.eventZaps().upsertAll(data = eventZaps)
        }
    }
}
