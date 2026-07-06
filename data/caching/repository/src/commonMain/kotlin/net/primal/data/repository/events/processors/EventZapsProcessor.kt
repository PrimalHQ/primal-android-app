package net.primal.data.repository.events.processors

import net.primal.data.local.db.CachingDatabase
import net.primal.data.remote.api.events.model.EventZapsResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.shared.data.local.db.withTransaction

suspend fun EventZapsResponse.persistToDatabaseAsTransaction(database: CachingDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource()
    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
    val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
    val profiles = this.profiles.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalPremiumInfo = primalPremiumInfo,
        primalLegendProfiles = primalLegendProfiles,
        blossomServers = blossomServers,
    )
    val eventZaps = this.zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.eventZaps().upsertAll(data = eventZaps)
    }
}
