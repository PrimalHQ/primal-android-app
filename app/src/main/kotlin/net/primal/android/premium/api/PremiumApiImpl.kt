package net.primal.android.premium.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.api.model.CancelMembershipRequest
import net.primal.android.premium.api.model.ChangeNameRequest
import net.primal.android.premium.api.model.MembershipProductsRequest
import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.NameAvailableRequest
import net.primal.android.premium.api.model.NameAvailableResponse
import net.primal.android.premium.api.model.PurchaseMembershipRequest
import net.primal.android.settings.api.model.AppSpecificDataRequest

class PremiumApiImpl @Inject constructor(
    @PrimalWalletApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : PremiumApi {

    override suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_NAME_AVAILABLE,
                optionsJson = NostrJson.encodeToString(NameAvailableRequest(name = name)),
            ),
        )

        val event = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalMembershipNameAvailable)
        return NostrJson.decodeFromStringOrNull<NameAvailableResponse>(event?.content)
            ?: throw WssException("Invalid content")
    }

    override suspend fun changePrimalName(userId: String, name: String): NameAvailableResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_CHANGE_NAME,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = NostrJson.encodeToString(ChangeNameRequest(name = name)),
                        ),
                    ),
                ),
            ),
        )

        val event = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalMembershipNameAvailable)
        return NostrJson.decodeFromStringOrNull<NameAvailableResponse>(event?.content)
            ?: throw WssException("Invalid content")
    }

    override suspend fun getPremiumMembershipStatus(userId: String): MembershipStatusResponse? {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_STATUS,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAuthorizationNostrEvent(
                            userId = userId,
                            description = "Check Primal Premium membership status",
                            tags = listOf(userId.asPubkeyTag()),
                        ),
                    ),
                ),
            ),
        )
        val statusEvent = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalMembershipStatus)
        return NostrJson.decodeFromStringOrNull<MembershipStatusResponse>(statusEvent?.content)
    }

    override suspend fun purchaseMembership(userId: String, body: PurchaseMembershipRequest) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_PURCHASE_MEMBERSHIP,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = NostrJson.encodeToString(body),
                        ),
                    ),
                ),
            ),
        )
    }

    override suspend fun getMembershipProducts() {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_PRODUCTS,
                optionsJson = NostrJson.encodeToString(MembershipProductsRequest(origin = "android")),
            ),
        )

//        "kind": 10000604,
//        [
//            {
//                "product_id": "1-month-premium",
//                "tier": "premium",
//                "months": 1,
//                "max_storage": 107374182400,
//                "label": "Primal Premium 1M Subscription",
//                "android_product_id": "monthly_premium",
//                "short_product_id": "1M"
//            },
//            {
//                "product_id": "12-months-premium",
//                "tier": "premium",
//                "months": 12,
//                "max_storage": 107374182400,
//                "label": "Primal Premium 12M Subscription",
//                "android_product_id": "yearly_premium",
//                "short_product_id": "12M"
//            }
//        ]
    }

    override suspend fun cancelMembership(userId: String, body: CancelMembershipRequest) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_CANCEL,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = NostrJson.encodeToString(body),
                        ),
                    ),
                ),
            ),
        )
    }
}
