package net.primal.data.account.remote.pushnotifications

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.remote.PrimalAccountVerb
import net.primal.data.account.remote.pushnotifications.model.UpdateNotificationTokenRequest
import net.primal.domain.nostr.NostrEvent

class PushNotificationApiImpl(
    private val primalApiClient: PrimalApiClient,
) : PushNotificationApi {

    override suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalAccountVerb.UPDATE_PUSH_NOTIFICATION_TOKEN.id,
                optionsJson = UpdateNotificationTokenRequest(
                    authorizationEvents = authorizationEvents,
                    platform = "android",
                    token = token,
                ).encodeToJsonString(),
            ),
        )
    }

    override suspend fun updateNotificationTokenForNip46(authorizationEvent: NostrEvent, token: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalAccountVerb.UPDATE_PUSH_NOTIFICATION_TOKEN_FOR_NIP46.id,
                optionsJson = UpdateNotificationTokenRequest(
                    authorizationEvents = listOf(authorizationEvent),
                    platform = "android",
                    token = token,
                ).encodeToJsonString(),
            ),
        )
    }
}
