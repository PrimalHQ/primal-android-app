package net.primal.android.core.push.api

import javax.inject.Inject
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.domain.nostr.NostrEvent

class PrimalPushMessagesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalCacheApiClient: PrimalApiClient,
) : PrimalPushMessagesApi {

    override suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String) {
        primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.UPDATE_PUSH_NOTIFICATION_TOKEN.id,
                optionsJson = UpdateNotificationTokenRequest(
                    authorizationEvents = authorizationEvents,
                    platform = "android",
                    token = token,
                ).encodeToJsonString(),
            ),
        )
    }
}
