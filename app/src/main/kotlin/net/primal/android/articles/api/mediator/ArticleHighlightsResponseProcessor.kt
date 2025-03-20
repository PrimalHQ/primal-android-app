package net.primal.android.articles.api.mediator

import androidx.room.withTransaction
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.highlights.utils.mapNotNullAsHighlightComments
import net.primal.android.nostr.db.eventRelayHintsUpserter
import net.primal.android.nostr.ext.asHighlightData
import net.primal.android.nostr.ext.flatMapAsEventHintsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsEventZapDO
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.mapNotNullAsEventStatsPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.data.remote.api.articles.ArticleHighlightsResponse

suspend fun ArticleHighlightsResponse.persistToDatabaseAsTransaction(database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
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
