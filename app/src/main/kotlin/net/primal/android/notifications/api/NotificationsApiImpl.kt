package net.primal.android.notifications.api

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformWhile
import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.nostr.ext.asNotificationSummary
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.api.model.NotificationsResponse
import net.primal.android.notifications.api.model.PubkeyRequestBody
import net.primal.android.notifications.domain.NotificationsSummary
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSpecificDataRequest
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class NotificationsApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
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

    override suspend fun updateLastSeenTimestamp(userId: String) {
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

    override suspend fun getNotifications(userId: String): NotificationsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_NOTIFICATIONS,
                optionsJson = NostrJson.encodeToString(
                    NotificationsRequestBody(
                        pubkey = userId,
                        userPubkey = userId,
                    )
                )
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

    override suspend fun getNotificationsSummary(userId: String): NotificationsSummary? {
        val subscriptionId = UUID.randomUUID()
        val firstEventMessage: NostrIncomingMessage.EventMessage = primalApiClient.subscribe(
            subscriptionId = subscriptionId,
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.NEW_NOTIFICATIONS_COUNT,
                optionsJson = NostrJson.encodeToString(PubkeyRequestBody(pubkey = userId))
            )
        ).transformWhile {
            if (it is NostrIncomingMessage.EventMessage) {
                emit(it)
                false
            } else {
                true
            }
        }.first()
        primalApiClient.closeSubscription(subscriptionId = subscriptionId)

        val summaryEvent = firstEventMessage.primalEvent
        return when (summaryEvent?.kind) {
            NostrEventKind.PrimalNotificationsSummary2.value -> summaryEvent.asNotificationSummary()
            else -> null
        }
    }
}
