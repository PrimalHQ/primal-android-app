package net.primal.data.repository.importer

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndFoldPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndFoldPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndFoldPrimalUserNames
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.repository.CachingImportRepository

class CachingImportRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
) : CachingImportRepository {

    override suspend fun cacheNostrEvents(vararg events: NostrEvent) {
        cacheEvents(nostrEvents = events.toList(), primalEvents = emptyList())
    }

    override suspend fun cacheNostrEvents(events: List<NostrEvent>) {
        cacheEvents(nostrEvents = events, primalEvents = emptyList())
    }

    override suspend fun cachePrimalEvents(vararg events: PrimalEvent) {
        cacheEvents(nostrEvents = emptyList(), primalEvents = events.toList())
    }

    override suspend fun cachePrimalEvents(events: List<PrimalEvent>) {
        cacheEvents(nostrEvents = emptyList(), primalEvents = events)
    }

    // TODO Implement logic for caching all other events
    override suspend fun cacheEvents(nostrEvents: List<NostrEvent>, primalEvents: List<PrimalEvent>) =
        withContext(dispatcherProvider.io()) {
            val primalUserNames = primalEvents
                .filter { it.kind == NostrEventKind.PrimalUserNames.value }
                .parseAndFoldPrimalUserNames()

            val primalPremiumInfo = primalEvents
                .filter { it.kind == NostrEventKind.PrimalPremiumInfo.value }
                .parseAndFoldPrimalPremiumInfo()

            val primalLegendProfiles = primalEvents
                .filter { it.kind == NostrEventKind.PrimalLegendProfiles.value }
                .parseAndFoldPrimalLegendProfiles()

            val cdnResources = primalEvents
                .filter { it.kind == NostrEventKind.PrimalCdnResource.value }
                .flatMapNotNullAsCdnResource()

            val blossomServers = nostrEvents
                .filter { it.kind == NostrEventKind.BlossomServerList.value }
                .mapAsMapPubkeyToListOfBlossomServers()

            val profilesPO = nostrEvents
                .filter { it.kind == NostrEventKind.Metadata.value }
                .mapAsProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNames,
                    primalPremiumInfo = primalPremiumInfo,
                    primalLegendProfiles = primalLegendProfiles,
                    blossomServers = blossomServers,
                )

            database.profiles().insertOrUpdateAll(data = profilesPO)
        }
}
