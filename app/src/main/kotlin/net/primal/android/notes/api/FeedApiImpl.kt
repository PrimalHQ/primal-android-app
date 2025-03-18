package net.primal.android.notes.api

import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.notes.api.model.FeedBySpecRequestBody
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.api.model.NotesRequestBody
import net.primal.android.notes.api.model.ThreadRequestBody
import net.primal.data.remote.PrimalVerb
import net.primal.domain.nostr.NostrEventKind
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalCacheFilter

class FeedApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : FeedApi {

    override suspend fun getFeedBySpec(body: FeedBySpecRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEGA_FEED_DIRECTIVE,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
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
                primalVerb = PrimalVerb.THREAD_VIEW,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
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
                primalVerb = PrimalVerb.EVENTS,
                optionsJson = NostrJson.encodeToString(
                    NotesRequestBody(noteIds = noteIds.toList(), extendedResponse = true),
                ),
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
