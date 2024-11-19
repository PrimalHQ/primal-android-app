package net.primal.android.notes.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.primal.PrimalVerb.NOTES
import net.primal.android.networking.primal.PrimalVerb.THREAD_VIEW
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.notes.api.model.FeedBySpecRequestBody
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.api.model.NotesRequestBody
import net.primal.android.notes.api.model.ThreadRequestBody

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
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
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
        )
    }

    override suspend fun getThread(body: ThreadRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = THREAD_VIEW,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
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
        )
    }

    override suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = NOTES,
                optionsJson = NostrJson.encodeToString(
                    NotesRequestBody(noteIds = noteIds.toList(), extendedResponse = true),
                ),
            ),
        )

        return FeedResponse(
            paging = null,
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
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
        )
    }
}
