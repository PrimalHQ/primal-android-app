package net.primal.data.remote.api.feeds

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.feeds.model.DvmFeedsRequestBody
import net.primal.data.remote.api.feeds.model.DvmFeedsResponse
import net.primal.data.remote.api.feeds.model.FeedsResponse
import net.primal.data.remote.api.feeds.model.SubSettingsAuthorization
import net.primal.data.remote.model.ContentAppSubSettings
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

internal class FeedsApiImpl(
    private val primalApiClient: PrimalApiClient,
) : FeedsApi {

    override suspend fun getFeaturedFeeds(specKind: FeedSpecKind?, pubkey: String?): DvmFeedsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_FEATURED_DVM_FEEDS.id,
                optionsJson = DvmFeedsRequestBody(
                    specKind = specKind?.id,
                    pubkey = pubkey,
                ).encodeToJsonString(),
            ),
        )

        return DvmFeedsResponse(
            scores = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalEventStats),
            dvmHandlers = queryResult.filterNostrEvents(kind = NostrEventKind.AppHandler),
            feedMetadata = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalDvmFeedMetadata),
            feedFollowActions = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalDvmFeedFollowsActions),
            feedUserStats = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalEventUserStats),
            userMetadata = queryResult.filterNostrEvents(kind = NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalCdnResource),
            userScores = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalUserScores),
            primalUserNames = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
        )
    }

    override suspend fun getDefaultUserFeeds(specKind: FeedSpecKind): FeedsResponse {
        val key = when (specKind) {
            FeedSpecKind.Reads -> "user-reads-feeds"
            FeedSpecKind.Notes -> "user-home-feeds"
        }
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_DEFAULT_APP_SUB_SETTINGS.id,
                optionsJson = ContentAppSubSettings<String>(key = key).encodeToJsonString(),
            ),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalSubSettings)
            ?: throw WssException("Invalid default feeds response for key '$key'.")

        return FeedsResponse(articleFeeds = articleFeeds)
    }

    override suspend fun getUserFeeds(authorization: NostrEvent, specKind: FeedSpecKind): FeedsResponse {
        val key = when (specKind) {
            FeedSpecKind.Reads -> "user-reads-feeds"
            FeedSpecKind.Notes -> "user-home-feeds"
        }

//        val signedNostrEvent = nostrNotary.signAppSpecificDataNostrEvent(
//            userId = userId,
//            content = ContentAppSubSettings<String>(key = key).encodeToJsonString(),
//        )

        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_APP_SUB_SETTINGS.id,
                optionsJson = SubSettingsAuthorization(event = authorization).encodeToJsonString(),
            ),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalSubSettings)
            ?: throw WssException("Invalid feeds response for key '$key'.")

        return FeedsResponse(articleFeeds = articleFeeds)
    }

    override suspend fun setUserFeeds(
//        userId: String,
//        specKind: FeedSpecKind,
//        feeds: List<ContentArticleFeedData>,
        userFeedsNostrEvent: NostrEvent,
    ) {
//        val signedNostrEvent = nostrNotary.signAppSpecificDataNostrEvent(
//            userId = userId,
//            content = ContentAppSubSettings(
//                key = when (specKind) {
//                    FeedSpecKind.Reads -> "user-reads-feeds"
//                    FeedSpecKind.Notes -> "user-home-feeds"
//                },
//                settings = feeds,
//            ).encodeToJsonString(),
//        )

        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_APP_SUB_SETTINGS.id,
                optionsJson = SubSettingsAuthorization(event = userFeedsNostrEvent).encodeToJsonString(),
            ),
        )
    }
}
