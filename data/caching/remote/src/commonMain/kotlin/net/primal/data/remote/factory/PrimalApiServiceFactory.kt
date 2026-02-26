package net.primal.data.remote.factory

import de.jensklingenberg.ktorfit.Ktorfit
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.remote.api.articles.ArticlesApi
import net.primal.data.remote.api.articles.ArticlesApiImpl
import net.primal.data.remote.api.broadcast.BroadcastApi
import net.primal.data.remote.api.broadcast.BroadcastApiImpl
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.EventStatsApiImpl
import net.primal.data.remote.api.explore.ExploreApi
import net.primal.data.remote.api.explore.ExploreApiImpl
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.FeedApiImpl
import net.primal.data.remote.api.feeds.FeedsApi
import net.primal.data.remote.api.feeds.FeedsApiImpl
import net.primal.data.remote.api.importing.PrimalImportApi
import net.primal.data.remote.api.importing.PrimalImportApiImpl
import net.primal.data.remote.api.klipy.KlipyApi
import net.primal.data.remote.api.klipy.KlipyApiImpl
import net.primal.data.remote.api.messages.MessagesApi
import net.primal.data.remote.api.messages.MessagesApiImpl
import net.primal.data.remote.api.notifications.NotificationsApi
import net.primal.data.remote.api.notifications.NotificationsApiImpl
import net.primal.data.remote.api.premium.PremiumBroadcastApi
import net.primal.data.remote.api.premium.PremiumBroadcastApiImpl
import net.primal.data.remote.api.settings.SettingsApi
import net.primal.data.remote.api.settings.SettingsApiImpl
import net.primal.data.remote.api.stream.LiveStreamApi
import net.primal.data.remote.api.stream.LiveStreamApiImpl
import net.primal.data.remote.api.users.UserWellKnownApi
import net.primal.data.remote.api.users.UsersApi
import net.primal.data.remote.api.users.UsersApiImpl
import net.primal.data.remote.api.users.createUserWellKnownApi

object PrimalApiServiceFactory {

    private val dispatcherProvider = createDispatcherProvider()
    private val defaultHttpClient = HttpClientFactory.createHttpClientWithDefaultConfig()

    fun createArticlesApi(primalApiClient: PrimalApiClient): ArticlesApi = ArticlesApiImpl(primalApiClient)

    fun createBroadcastApi(primalApiClient: PrimalApiClient): BroadcastApi = BroadcastApiImpl(primalApiClient)

    fun createPremiumBroadcastApi(primalApiClient: PrimalApiClient): PremiumBroadcastApi =
        PremiumBroadcastApiImpl(primalApiClient)

    fun createEventsApi(primalApiClient: PrimalApiClient): EventStatsApi = EventStatsApiImpl(primalApiClient)

    fun createExploreApi(primalApiClient: PrimalApiClient): ExploreApi = ExploreApiImpl(primalApiClient)

    fun createFeedApi(primalApiClient: PrimalApiClient): FeedApi = FeedApiImpl(primalApiClient)

    fun createFeedsApi(primalApiClient: PrimalApiClient): FeedsApi = FeedsApiImpl(primalApiClient)

    fun createImportApi(primalApiClient: PrimalApiClient): PrimalImportApi = PrimalImportApiImpl(primalApiClient)

    fun createMessagesApi(primalApiClient: PrimalApiClient): MessagesApi = MessagesApiImpl(primalApiClient)

    fun createNotificationsApi(primalApiClient: PrimalApiClient): NotificationsApi =
        NotificationsApiImpl(primalApiClient)

    fun createSettingsApi(primalApiClient: PrimalApiClient): SettingsApi = SettingsApiImpl(primalApiClient)

    fun createUsersApi(primalApiClient: PrimalApiClient): UsersApi = UsersApiImpl(primalApiClient)

    fun createKlipyApi(apiKey: String, clientKey: String): KlipyApi =
        KlipyApiImpl(
            apiKey = apiKey,
            clientKey = clientKey,
            httpClient = defaultHttpClient,
        )

    fun createUserWellKnownApi(): UserWellKnownApi =
        Ktorfit.Builder()
            .baseUrl("https://primal.net/")
            .httpClient(client = defaultHttpClient)
            .build()
            .createUserWellKnownApi()

    fun createStreamMonitor(primalApiClient: PrimalApiClient): LiveStreamApi =
        LiveStreamApiImpl(primalApiClient = primalApiClient)
}
