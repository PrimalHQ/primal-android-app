package net.primal.data.repository.articles.processors

import net.primal.data.local.dao.events.eventRelayHintsUpserter
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.articles.ArticleHighlightsResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.remote.asHighlightData
import net.primal.data.repository.mappers.remote.flatMapAsEventHintsPO
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsEventStatsPO
import net.primal.data.repository.mappers.remote.mapNotNullAsHighlightComments
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames

suspend fun ArticleHighlightsResponse.persistToDatabaseAsTransaction(database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource()
    val eventHints = this.relayHints.flatMapAsEventHintsPO()

    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = this.legendProfiles.parseAndMapPrimalLegendProfiles()

    val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

    val profiles = this.profileMetadatas.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalPremiumInfo = primalPremiumInfo,
        primalLegendProfiles = primalLegendProfiles,
        blossomServers = blossomServers,
    )

    val highlights = this.highlights.map { it.asHighlightData() }
    val highlightComments = this.highlightComments.mapNotNullAsHighlightComments(highlights = highlights)

    val eventZaps = this.zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    val eventStats = this.eventStats.mapNotNullAsEventStatsPO()

    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.posts().upsertAll(data = highlightComments)
        database.eventStats().upsertAll(data = eventStats)
        database.eventZaps().upsertAll(data = eventZaps)
        database.highlights().upsertAll(data = highlights)

        val eventHintsDao = database.eventHints()
        val hintsMap = eventHints.associateBy { it.eventId }
        eventRelayHintsUpserter(dao = eventHintsDao, eventIds = eventHints.map { it.eventId }) {
            copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
        }
    }
}
