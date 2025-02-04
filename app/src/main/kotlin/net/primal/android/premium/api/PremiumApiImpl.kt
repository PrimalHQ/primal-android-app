package net.primal.android.premium.api

import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.core.serialization.json.NostrJsonImplicitNulls
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.api.model.CancelMembershipRequest
import net.primal.android.premium.api.model.ChangeNameRequest
import net.primal.android.premium.api.model.LegendLeaderboardOrderBy
import net.primal.android.premium.api.model.LegendLeaderboardRequest
import net.primal.android.premium.api.model.LegendLeaderboardResponse
import net.primal.android.premium.api.model.LegendPaymentInstructionsResponse
import net.primal.android.premium.api.model.MembershipProductsRequest
import net.primal.android.premium.api.model.MembershipStatusResponse
import net.primal.android.premium.api.model.NameAvailableRequest
import net.primal.android.premium.api.model.NameAvailableResponse
import net.primal.android.premium.api.model.PremiumLeaderboardOrderBy
import net.primal.android.premium.api.model.PremiumLeaderboardRequest
import net.primal.android.premium.api.model.PremiumLeaderboardResponse
import net.primal.android.premium.api.model.PurchaseMembershipRequest
import net.primal.android.premium.api.model.ShowSupportUsResponse
import net.primal.android.premium.api.model.UpdatePrimalLegendProfileRequest
import net.primal.android.premium.domain.PremiumPurchaseOrder
import net.primal.android.settings.api.model.AppSpecificDataRequest

class PremiumApiImpl @Inject constructor(
    @PrimalWalletApiClient private val primalWalletApiClient: PrimalApiClient,
    @PrimalCacheApiClient private val primalCacheApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : PremiumApi {

    override suspend fun isPrimalNameAvailable(name: String): NameAvailableResponse {
        val queryResult = primalWalletApiClient.query(
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
        val queryResult = primalWalletApiClient.query(
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
        val queryResult = primalWalletApiClient.query(
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
        primalWalletApiClient.query(
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

    override suspend fun getPrimalLegendPaymentInstructions(
        userId: String,
        primalName: String,
    ): LegendPaymentInstructionsResponse {
        val result = primalWalletApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_PURCHASE_MEMBERSHIP,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = NostrJson.encodeToString(
                                PurchaseMembershipRequest(
                                    primalProductId = "legend-premium",
                                    name = primalName,
                                    receiverUserId = userId,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val event = result.findPrimalEvent(NostrEventKind.PrimalMembershipLegendPaymentInstructions)
        return event?.takeContentOrNull<LegendPaymentInstructionsResponse>()
            ?: throw WssException("Missing event or invalid content.")
    }

    override suspend fun getMembershipProducts() {
        primalWalletApiClient.query(
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
        primalWalletApiClient.query(
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

    override suspend fun shouldShowSupportUs(): Boolean {
        val result = primalCacheApiClient.query(message = PrimalCacheFilter(primalVerb = PrimalVerb.CLIENT_CONFIG))
        val configEvent = result.findPrimalEvent(NostrEventKind.PrimalClientConfig)
        val response = NostrJson.decodeFromStringOrNull<ShowSupportUsResponse>(configEvent?.content)
        return response?.showSupportPrimal == true
    }

    override suspend fun getOrdersHistory(userId: String): List<PremiumPurchaseOrder> {
        val queryResult = primalWalletApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_PURCHASE_HISTORY,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{\"since\": 0, \"limit\": 100}",
                        ),
                    ),
                ),
            ),
        )

        val historyEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalMembershipHistory)
        return historyEvent.takeContentOrNull<List<PremiumPurchaseOrder>>()
            ?: throw WssException("Missing event or invalid content.")
    }

    override suspend fun updateLegendProfile(userId: String, updateProfileRequest: UpdatePrimalLegendProfileRequest) {
        primalWalletApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_LEGEND_CUSTOMIZATION,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = NostrJsonImplicitNulls.encodeToString(updateProfileRequest),
                        ),
                    ),
                ),
            ),
        )
    }

    override suspend fun getLegendLeaderboard(
        orderBy: LegendLeaderboardOrderBy,
        limit: Int,
    ): LegendLeaderboardResponse {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_LEGENDS_LEADERBOARD,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    LegendLeaderboardRequest(
                        orderBy = orderBy,
                        limit = limit,
                    ),
                ),
            ),
        )

        return LegendLeaderboardResponse(
            orderedLegendLeaderboardEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendLeaderboard),
            primalPremiumInfoEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalPremiumInfo),
            primalLegendProfiles = queryResult.filterPrimalEvents(NostrEventKind.PrimalLegendProfiles),
            primalUsernames = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserNames),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            userFollowersCounts = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserFollowersCounts),
            userScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            profileMetadatas = queryResult.filterNostrEvents(NostrEventKind.Metadata),
        )
    }

    override suspend fun getPremiumLeaderboard(
        orderBy: PremiumLeaderboardOrderBy,
        limit: Int,
    ): PremiumLeaderboardResponse {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_PREMIUM_LEADERBOARD,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    PremiumLeaderboardRequest(
                        orderBy = orderBy,
                        limit = limit,
                    ),
                ),
            ),
        )

        return PremiumLeaderboardResponse(
            orderedPremiumLeaderboardEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumLeaderboard),
            primalPremiumInfoEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalPremiumInfo),
            primalUsernames = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserNames),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            userFollowersCounts = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserFollowersCounts),
            userScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            profileMetadatas = queryResult.filterNostrEvents(NostrEventKind.Metadata),
        )
    }

    override suspend fun getRecoveryContactsList(userId: String): List<NostrEvent> {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_RECOVERY_CONTACT_LISTS,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "",
                        ),
                    ),
                ),
            ),
        )

        return queryResult.filterNostrEvents(NostrEventKind.FollowList)
    }
}
