package net.primal.android.note.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.note.api.model.NoteActionsRequestBody
import net.primal.android.note.api.model.NoteActionsResponse
import net.primal.android.note.api.model.NoteZapsRequestBody
import net.primal.android.note.api.model.NoteZapsResponse

class NoteApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : NoteApi {

    override suspend fun getNoteZaps(body: NoteZapsRequestBody): NoteZapsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.EVENT_ZAPS,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return NoteZapsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            profiles = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
        )
    }

    override suspend fun getNoteActions(body: NoteActionsRequestBody): NoteActionsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.EVENT_ACTIONS,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return NoteActionsResponse(
            profiles = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            userFollowersCount = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
        )
    }
}
