package net.primal.android.notifications.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.api.model.NotificationsResponse
import net.primal.android.notifications.api.model.PubkeyRequestBody
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSpecificDataRequest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

class NotificationsApiImpl @Inject constructor(
    @Named("Api") private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : NotificationsApi {

    override suspend fun getLastSeenTimestamp(userId: String): Instant? {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_LAST_SEEN_NOTIFICATIONS,
                optionsJson = NostrJson.encodeToString(PubkeyRequestBody(pubkey = userId))
            )
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
                        eventFromUser = nostrNotary.signAppSettingsSyncNostrEvent(
                            userId = userId,
                            description = "Update notifications last seen timestamp.",
                        ),
                    )
                ),
            )
        )
    }

    override suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_NOTIFICATIONS,
                optionsJson = NostrJson.encodeToString(body),
            )
        )

        return NotificationsResponse(
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            primalNoteStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalMediaResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
            primalReferencedNotes = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalUserProfileStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserProfileStats),
            primalNotifications = queryResult.filterPrimalEvents(NostrEventKind.PrimalNotification),
        )
    }
}
