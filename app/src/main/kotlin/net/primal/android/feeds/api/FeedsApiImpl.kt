package net.primal.android.feeds.api

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.feeds.api.model.FeedsResponse
import net.primal.android.feeds.api.model.SubSettingsAuthorization
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.primal.PrimalVerb.GET_FEATURED_DVM_READS
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.content.ContentAppSubSettings
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData
import net.primal.android.nostr.notary.NostrNotary

@Singleton
class FeedsApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : FeedsApi {

    override suspend fun getFeaturedReadsFeeds(): DvmFeedsResponse {
        return queryDvmFeeds(kind = "reads")
    }

    override suspend fun getFeaturedHomeFeeds(): DvmFeedsResponse {
        return queryDvmFeeds(kind = "notes")
    }

    override suspend fun getDefaultReadsUserFeeds(): FeedsResponse {
        return queryDefaultUserFeeds(key = "user-reads-feeds")
    }

    override suspend fun getReadsUserFeeds(userId: String): FeedsResponse {
        return queryUserFeeds(userId = userId, key = "user-reads-feeds", verb = PrimalVerb.GET_APP_SUB_SETTINGS)
    }

    override suspend fun setReadsUserFeeds(userId: String, feeds: List<ContentArticleFeedData>) {
        setUserFeeds(userId = userId, feeds = feeds, key = "user-reads-feeds")
    }

    override suspend fun getDefaultHomeUserFeeds(): FeedsResponse {
        return queryDefaultUserFeeds(key = "user-home-feeds")
    }

    override suspend fun getHomeUserFeeds(userId: String): FeedsResponse {
        return queryUserFeeds(userId = userId, key = "user-home-feeds", verb = PrimalVerb.GET_APP_SUB_SETTINGS)
    }

    override suspend fun setHomeUserFeeds(userId: String, feeds: List<ContentArticleFeedData>) {
        setUserFeeds(userId = userId, feeds = feeds, key = "user-home-feeds")
    }

    private suspend fun queryDvmFeeds(kind: String): DvmFeedsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = GET_FEATURED_DVM_READS,
                optionsJson = NostrJson.encodeToString(DvmFeedsRequestBody(kind = kind)),
            ),
        )

        return DvmFeedsResponse(
            scores = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalEventStats),
            dvmHandlers = queryResult.filterNostrEvents(kind = NostrEventKind.AppHandler),
        )
    }

    private suspend fun queryDefaultUserFeeds(key: String): FeedsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_DEFAULT_APP_SUB_SETTINGS,
                optionsJson = NostrJson.encodeToString(ContentAppSubSettings<String>(key = key)),
            ),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalSubSettings)
            ?: throw WssException("Invalid default feeds response for key '$key'.")

        return FeedsResponse(articleFeeds = articleFeeds)
    }

    private suspend fun queryUserFeeds(
        userId: String,
        key: String,
        verb: PrimalVerb,
    ): FeedsResponse {
        val signedNostrEvent = nostrNotary.signAppSpecificDataNostrEvent(
            userId = userId,
            content = NostrJson.encodeToString(
                ContentAppSubSettings<String>(key = key),
            ),
        )

        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = verb,
                optionsJson = NostrJson.encodeToString(SubSettingsAuthorization(event = signedNostrEvent)),
            ),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalSubSettings)
            ?: throw WssException("Invalid feeds response for key '$key'.")

        return FeedsResponse(articleFeeds = articleFeeds)
    }

    private suspend fun setUserFeeds(
        userId: String,
        key: String,
        feeds: List<ContentArticleFeedData>,
    ) {
        val signedNostrEvent = nostrNotary.signAppSpecificDataNostrEvent(
            userId = userId,
            content = NostrJson.encodeToString(
                ContentAppSubSettings(key = key, settings = feeds),
            ),
        )

        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_APP_SUB_SETTINGS,
                optionsJson = NostrJson.encodeToString(SubSettingsAuthorization(event = signedNostrEvent)),
            ),
        )
    }
}
