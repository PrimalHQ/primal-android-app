package net.primal.data.account.remote.blossom

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.PrimalAccountVerb
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind

class BlossomsApiImpl(
    private val primalApiClient: PrimalApiClient,
) : BlossomsApi {
    override suspend fun getRecommendedBlossomServers(): List<String> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = PrimalAccountVerb.RECOMMENDED_BLOSSOM_SERVERS.id),
        )

        val list = queryResult.findPrimalEvent(NostrEventKind.PrimalRecommendedBlossomServer)
        val content = list?.content
        if (content.isNullOrEmpty()) throw NetworkException("Invalid content.")

        return list.content.decodeFromJsonStringOrNull<List<String>>() ?: emptyList()
    }
}
