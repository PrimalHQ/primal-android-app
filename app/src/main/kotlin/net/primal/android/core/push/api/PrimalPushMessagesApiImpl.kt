package net.primal.android.core.push.api

import javax.inject.Inject
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb

class PrimalPushMessagesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalCacheApiClient: PrimalApiClient,
) : PrimalPushMessagesApi {

    override suspend fun updateNotificationsToken(userIds: List<String>, token: String) {
        primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.UPDATE_NOTIFICATION_TOKEN.id,
                optionsJson = UpdateNotificationTokenRequest(
                    userIds = userIds,
                    platform = "android",
                    token = token,
                ).encodeToJsonString(),
            ),
        )
    }
}
