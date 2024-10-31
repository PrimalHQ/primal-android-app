package net.primal.android.premium.api

import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.premium.api.model.NameAvailableRequest
import net.primal.android.premium.api.model.NameAvailableResponse

class PremiumApiImpl constructor(
    @PrimalWalletApiClient private val primalApiClient: PrimalApiClient,
) : PremiumApi {
    override suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_NAME_AVAILABLE,
                optionsJson = NostrJson.encodeToString(NameAvailableRequest(name = name)),
            ),
        )

        return NameAvailableResponse(
            membershipAvailableEvent = queryResult.findPrimalEvent(kind = NostrEventKind.MembershipNameAvailable),
        )
    }
}
