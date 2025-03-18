package net.primal.android.notifications.api

import java.time.Instant
import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.api.model.NotificationsResponse
import net.primal.android.notifications.api.model.PubkeyRequestBody
import net.primal.android.settings.api.model.AppSpecificDataRequest
import net.primal.data.remote.PrimalVerb
import net.primal.domain.nostr.NostrEventKind
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalCacheFilter

class NotificationsApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : NotificationsApi {

    override suspend fun getLastSeenTimestamp(userId: String): Instant? {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_LAST_SEEN_NOTIFICATIONS,
                optionsJson = NostrJson.encodeToString(PubkeyRequestBody(pubkey = userId)),
            ),
        )

        val notificationsSeenEvent = queryResult
            .filterPrimalEvents(NostrEventKind.PrimalNotificationsSeenUntil)
            .firstOrNull()

        val seenTimestampInSeconds = notificationsSeenEvent?.content?.toLongOrNull()
        return if (seenTimestampInSeconds != null) {
            Instant.ofEpochSecond(seenTimestampInSeconds)
        } else {
            null
        }
    }

    override suspend fun setLastSeenTimestamp(userId: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_LAST_SEEN_NOTIFICATIONS,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAuthorizationNostrEvent(
                            userId = userId,
                            description = "Update notifications last seen timestamp.",
                        ),
                    ),
                ),
            ),
        )
    }

    override suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_NOTIFICATIONS,
                optionsJson = NostrJson.encodeToString(body),
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
