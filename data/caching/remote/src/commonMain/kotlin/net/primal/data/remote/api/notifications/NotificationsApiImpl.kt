package net.primal.data.remote.api.notifications

import kotlinx.datetime.Instant
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.notifications.model.NotificationsRequestBody
import net.primal.data.remote.api.notifications.model.NotificationsResponse
import net.primal.data.remote.api.notifications.model.PubkeyRequestBody
import net.primal.data.remote.model.AppSpecificDataRequest
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class NotificationsApiImpl(
    private val primalApiClient: PrimalApiClient,
) : NotificationsApi {

    override suspend fun getLastSeenTimestamp(userId: String): Instant? {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_LAST_SEEN_NOTIFICATIONS.id,
                optionsJson = PubkeyRequestBody(pubkey = userId).encodeToJsonString(),
            ),
        )

        val notificationsSeenEvent = queryResult
            .filterPrimalEvents(NostrEventKind.PrimalNotificationsSeenUntil)
            .firstOrNull()

        val seenTimestampInSeconds = notificationsSeenEvent?.content?.toLongOrNull()
        return if (seenTimestampInSeconds != null) {
            Instant.fromEpochSeconds(seenTimestampInSeconds)
        } else {
            null
        }
    }

    override suspend fun setLastSeenTimestamp(authorization: NostrEvent) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_LAST_SEEN_NOTIFICATIONS.id,
                optionsJson = AppSpecificDataRequest(authorization).encodeToJsonString(),
            ),
        )
    }

    override suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_NOTIFICATIONS.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return NotificationsResponse(
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            primalNoteStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalReferencedNotes = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalUserProfileStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserProfileStats),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            primalNotifications = queryResult.filterPrimalEvents(NostrEventKind.PrimalNotification),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }
}
