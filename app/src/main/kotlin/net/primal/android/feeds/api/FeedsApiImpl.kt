package net.primal.android.feeds.api

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.feeds.api.model.FeedsResponse
import net.primal.android.feeds.api.model.SubSettingsAuthorization
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.model.primal.content.ContentAppSubSettings
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.errors.WssException
import net.primal.data.remote.PrimalVerb
import net.primal.domain.nostr.NostrEventKind

@Singleton
class FeedsApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : FeedsApi {

    override suspend fun getFeaturedFeeds(specKind: FeedSpecKind?, pubkey: String?): DvmFeedsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_FEATURED_DVM_FEEDS.id,
                optionsJson = NostrJson.encodeToString(
                    DvmFeedsRequestBody(
                        specKind = specKind?.id,
                        pubkey = pubkey,
                    ),
                ),
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
                optionsJson = NostrJson.encodeToString(ContentAppSubSettings<String>(key = key)),
            ),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalSubSettings)
            ?: throw WssException("Invalid default feeds response for key '$key'.")

        return FeedsResponse(articleFeeds = articleFeeds)
    }

    override suspend fun getUserFeeds(userId: String, specKind: FeedSpecKind): FeedsResponse {
        val key = when (specKind) {
            FeedSpecKind.Reads -> "user-reads-feeds"
            FeedSpecKind.Notes -> "user-home-feeds"
        }

        val signedNostrEvent = nostrNotary.signAppSpecificDataNostrEvent(
            userId = userId,
            content = NostrJson.encodeToString(
                ContentAppSubSettings<String>(key = key),
            ),
        )

        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_APP_SUB_SETTINGS.id,
                optionsJson = NostrJson.encodeToString(SubSettingsAuthorization(event = signedNostrEvent)),
            ),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalSubSettings)
            ?: throw WssException("Invalid feeds response for key '$key'.")

        return FeedsResponse(articleFeeds = articleFeeds)
    }

    override suspend fun setUserFeeds(
        userId: String,
        specKind: FeedSpecKind,
        feeds: List<ContentArticleFeedData>,
    ) {
        val signedNostrEvent = nostrNotary.signAppSpecificDataNostrEvent(
            userId = userId,
            content = NostrJson.encodeToString(
                ContentAppSubSettings(
                    key = when (specKind) {
                        FeedSpecKind.Reads -> "user-reads-feeds"
                        FeedSpecKind.Notes -> "user-home-feeds"
                    },
                    settings = feeds,
                ),
            ),
        )

        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_APP_SUB_SETTINGS.id,
                optionsJson = NostrJson.encodeToString(SubSettingsAuthorization(event = signedNostrEvent)),
            ),
        )
    }
}
