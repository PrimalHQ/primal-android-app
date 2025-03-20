package net.primal.data.remote.api.feed

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.feed.model.FeedBySpecRequestBody
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.api.feed.model.NotesRequestBody
import net.primal.data.remote.api.feed.model.ThreadRequestBody
import net.primal.domain.nostr.NostrEventKind

internal class FeedApiImpl(
    private val primalApiClient: PrimalApiClient,
) : FeedApi {

    override suspend fun getFeedBySpec(body: FeedBySpecRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEGA_FEED_DIRECTIVE.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging)?.content.decodeFromJsonStringOrNull(),
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            articles = emptyList(),
            reposts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNoteRepost),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
            genericReposts = queryResult.filterNostrEvents(NostrEventKind.GenericRepost),
            pictureNotes = queryResult.filterNostrEvents(NostrEventKind.PictureNote),
        )
    }

    override suspend fun getThread(body: ThreadRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.THREAD_VIEW.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging)?.content?.decodeFromJsonStringOrNull(),
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            articles = queryResult.filterNostrEvents(NostrEventKind.LongFormContent),
            reposts = emptyList(),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
            genericReposts = emptyList(),
            pictureNotes = queryResult.filterNostrEvents(NostrEventKind.PictureNote),
        )
    }

    override suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.EVENTS.id,
                optionsJson = NotesRequestBody(
                    noteIds = noteIds.toList(),
                    extendedResponse = true,
                ).encodeToJsonString(),
            ),
        )

        return FeedResponse(
            paging = null,
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            articles = queryResult.filterNostrEvents(NostrEventKind.LongFormContent),
            reposts = emptyList(),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
            genericReposts = emptyList(),
            pictureNotes = queryResult.filterNostrEvents(NostrEventKind.PictureNote),
        )
    }
}
