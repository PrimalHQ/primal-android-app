package net.primal.android.premium.manage.content.api

import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.manage.content.api.model.BroadcastingStatus
import net.primal.android.premium.manage.content.api.model.ContentEventKindCount
import net.primal.android.premium.manage.content.api.model.StartContentBroadcastRequest
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.model.AppSpecificDataRequest
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow

class BroadcastApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalCacheApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : BroadcastApi {

    override suspend fun getContentStats(userId: String): Map<Int, Long> {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEMBERSHIP_CONTENT_STATS.id,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ).unwrapOrThrow(),
                    ),
                ),
            ),
        )

        val event = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalContentBroadcastStats)
        val counts = event.takeContentOrNull<List<ContentEventKindCount>>()
        return counts?.associate { it.kind to it.count } ?: throw NetworkException("Missing or invalid content")
    }

    override suspend fun startContentRebroadcast(userId: String, kinds: List<Int>?) {
        primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_START.id,
                optionsJson = StartContentBroadcastRequest(
                    eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                        userId = userId,
                        content = "{}",
                    ).unwrapOrThrow(),
                    kinds = kinds,
                ).encodeToJsonString(),
            ),
        )
    }

    override suspend fun cancelContentRebroadcast(userId: String) {
        primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_CANCEL.id,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ).unwrapOrThrow(),
                    ),
                ),
            ),
        )
    }

    override suspend fun getContentRebroadcastStatus(userId: String): BroadcastingStatus {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_STATUS.id,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ).unwrapOrThrow(),
                    ),
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalContentBroadcastStatus)
            ?.takeContentOrNull<BroadcastingStatus>()
            ?: throw NetworkException("Missing or invalid content")
    }
}
