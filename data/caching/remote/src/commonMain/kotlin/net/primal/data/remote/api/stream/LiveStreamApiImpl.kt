package net.primal.data.remote.api.stream

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.stream.model.LiveFeedRequestBody
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEventKind

class LiveStreamApiImpl(
    private val primalApiClient: PrimalApiClient,
) : LiveStreamApi {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun subscribe(streamingNaddr: Naddr, userId: String): Flow<LiveFeedResponse> {
        val subscriptionId = Uuid.random().toPrimalSubscriptionId()
        return primalApiClient
            .subscribeBuffered(
                subscriptionId = subscriptionId,
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.LIVE_FEED.id,
                    optionsJson = LiveFeedRequestBody(
                        kind = NostrEventKind.LiveActivity.value,
                        pubkey = streamingNaddr.userId,
                        identifier = streamingNaddr.identifier,
                        userPubkey = userId,
                    ).encodeToJsonString(),
                ),
            )
            .onCompletion {
                runCatching {
                    primalApiClient.closeSubscription(subscriptionId)
                }
            }
            .map {
                LiveFeedResponse(
                    zaps = it.filterNostrEvents(NostrEventKind.Zap),
                )
            }
    }
}
