package net.primal.android.settings.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.nostr.model.primal.content.ContentZapConfigItem
import net.primal.android.nostr.model.primal.content.ContentZapDefault
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_CONFIG
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_DEFAULT
import net.primal.android.settings.api.SettingsApi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount

class SettingsRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val settingsApi: SettingsApi,
    private val database: PrimalDatabase,
    private val accountsStore: UserAccountsStore,
) {
    suspend fun fetchAndPersistAppSettings(userId: String) {
        val appSettings = fetchAppSettings(userId = userId) ?: return
        val persistedAppSettings = persistAppSettingsLocally(userId = userId, appSettings = appSettings)

        val uniqueUserFeedsCount = appSettings.feeds.distinctBy { "${it.directive}${it.includeReplies}" }.count()
        if (appSettings.feeds.size > uniqueUserFeedsCount) {
            settingsApi.setAppSettings(userId = userId, appSettings = persistedAppSettings)
        }
    }

    suspend fun updateAndPersistZapDefault(userId: String, zapDefault: ContentZapDefault) {
        fetchAndUpdateAndPublishAppSettings(userId = userId) {
            copy(zapDefault = zapDefault)
        }
    }

    suspend fun updateAndPersistZapPresetsConfig(
        userId: String,
        presetIndex: Int,
        zapPreset: ContentZapConfigItem,
    ) {
        fetchAndUpdateAndPublishAppSettings(userId = userId) {
            copy(
                zapsConfig = this.zapsConfig.toMutableList().apply {
                    this[presetIndex] = zapPreset
                },
            )
        }
    }

    suspend fun updateAndPersistNotifications(userId: String, notifications: JsonObject) {
        fetchAndUpdateAndPublishAppSettings(userId = userId) {
            copy(notifications = notifications)
        }
    }

    suspend fun addAndPersistUserFeed(
        userId: String,
        name: String,
        directive: String,
    ) {
        fetchAndUpdateAndPublishAppSettings(userId = userId) {
            copy(
                feeds = feeds.toMutableList().apply {
                    add(ContentFeedData(name = name, directive = directive))
                },
            )
        }
    }

    suspend fun removeAndPersistUserFeed(userId: String, directive: String) {
        updateAndPublishAppSettings(userId = userId) {
            copy(
                feeds = feeds.toMutableList().apply {
                    removeAll { it.directive == directive }
                },
            )
        }
    }

    suspend fun reorderAndPersistUserFeeds(userId: String, newOrder: List<ContentFeedData>) {
        updateAndPublishAppSettings(userId = userId) {
            val feedsMap = this.feeds.associateBy { it.stableId() }
            val newFeedOrder = newOrder.mapNotNull { feedsMap[it.stableId()] }
            copy(feeds = newFeedOrder)
        }
    }

    private fun ContentFeedData.stableId() = Pair(this.name, this.directive)

    private suspend fun updateAndPersistFeeds(userId: String, feeds: List<ContentFeedData>) {
        fetchAndUpdateAndPublishAppSettings(userId = userId) {
            copy(feeds = feeds)
        }
    }

    suspend fun restoreDefaultUserFeeds(userId: String) {
        withContext(dispatcherProvider.io()) {
            val remoteDefaultAppSettings = fetchDefaultAppSettings(userId = userId) ?: return@withContext
            updateAndPersistFeeds(userId = userId, feeds = remoteDefaultAppSettings.feeds)
        }
    }

    @Suppress("MagicNumber")
    suspend fun ensureZapConfig(userId: String) {
        val userSettings = accountsStore.findByIdOrNull(userId)?.appSettings
        if (userSettings?.zapDefault != null && userSettings.zapsConfig.isNotEmpty()) return

        val defaultSettings = withContext(dispatcherProvider.io()) {
            fetchDefaultAppSettings(userId = userId)
        }
        val defaultZapDefault = defaultSettings?.zapDefault ?: DEFAULT_ZAP_DEFAULT
        val defaultZapsConfig = defaultSettings?.zapsConfig ?: DEFAULT_ZAP_CONFIG

        val existingZapDefaultValue = userSettings?.defaultZapAmount
        val existingZapsConfigValues = userSettings?.zapOptions

        fetchAndUpdateAndPublishAppSettings(userId = userId) {
            this.copy(
                zapDefault = defaultZapDefault.copy(
                    amount = existingZapDefaultValue?.toLong() ?: defaultZapDefault.amount,
                ),
                zapsConfig = if (existingZapsConfigValues.isNullOrEmpty()) {
                    defaultZapsConfig
                } else {
                    defaultZapsConfig.toMutableList().apply {
                        this[0] = this[0].copy(amount = existingZapsConfigValues[0].toLong())
                        this[1] = this[1].copy(amount = existingZapsConfigValues[1].toLong())
                        this[2] = this[2].copy(amount = existingZapsConfigValues[2].toLong())
                        this[3] = this[3].copy(amount = existingZapsConfigValues[3].toLong())
                        this[4] = this[4].copy(amount = existingZapsConfigValues[4].toLong())
                        this[5] = this[5].copy(amount = existingZapsConfigValues[5].toLong())
                    }
                },
            )
        }
    }

    private suspend fun fetchAndUpdateAndPublishAppSettings(
        userId: String,
        reducer: ContentAppSettings.() -> ContentAppSettings,
    ) {
        withContext(dispatcherProvider.io()) {
            val remoteAppSettings = fetchAppSettings(userId = userId) ?: return@withContext
            persistAppSettingsLocally(userId = userId, appSettings = remoteAppSettings)
            updateAndPublishAppSettings(userId = userId, reducer = reducer)
        }
    }

    private suspend fun updateAndPublishAppSettings(
        userId: String,
        reducer: ContentAppSettings.() -> ContentAppSettings,
    ) {
        withContext(dispatcherProvider.io()) {
            val localAppSettings = accountsStore.findByIdOrNull(userId = userId)?.appSettings ?: return@withContext
            val newAppSettings = localAppSettings.reducer()
            publishAppSettings(userId = userId, newAppSettings = newAppSettings)
        }
    }

    private suspend fun publishAppSettings(userId: String, newAppSettings: ContentAppSettings) {
        settingsApi.setAppSettings(userId = userId, appSettings = newAppSettings)
        persistAppSettingsLocally(userId = userId, appSettings = newAppSettings)
    }

    private suspend fun fetchAppSettings(userId: String): ContentAppSettings? {
        val response = settingsApi.getAppSettings(pubkey = userId)
        return NostrJson.decodeFromStringOrNull<ContentAppSettings>(
            string = response.userSettings?.content ?: response.defaultSettings?.content,
        )
    }

    private suspend fun fetchDefaultAppSettings(userId: String): ContentAppSettings? {
        val response = settingsApi.getDefaultAppSettings(pubkey = userId)
        return NostrJson.decodeFromStringOrNull<ContentAppSettings>(
            string = response.defaultSettings?.content,
        )
    }

    private suspend fun persistAppSettingsLocally(userId: String, appSettings: ContentAppSettings): ContentAppSettings {
        val currentUserAccount = accountsStore.findByIdOrNull(userId = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        val userFeeds = appSettings.feeds
            .distinctBy { "${it.directive}${it.includeReplies}" }
            .mapNotNull { it.asFeedPO() }
            .toMutableList()

        val hasLatestFeed = userFeeds.hasLatestFeed(userId)
        val hasLatestWithRepliesFeed = userFeeds.hasLatestWithRepliesFeed(userId)

        if (!hasLatestFeed && !hasLatestWithRepliesFeed) {
            userFeeds.add(0, Feed(directive = userId, name = "Latest"))
            userFeeds.add(0, Feed(directive = userId.toLatestWithRepliesDirective(), name = "Latest With Replies"))
        } else if (!hasLatestFeed) {
            userFeeds.add(0, Feed(directive = userId, name = "Latest"))
        } else if (!hasLatestWithRepliesFeed) {
            userFeeds.add(0, Feed(directive = userId.toLatestWithRepliesDirective(), name = "Latest With Replies"))
        }

        val localAppSettings = appSettings.copy(feeds = userFeeds.map { it.asContentFeedData() })
        accountsStore.upsertAccount(userAccount = currentUserAccount.copy(appSettings = localAppSettings))

        database.withTransaction {
            database.feeds().deleteAll()
            database.feeds().upsertAll(data = userFeeds)
        }

        return localAppSettings
    }

    private fun List<Feed>.hasLatestFeed(userId: String) = find { it.isLatest(userId) } != null

    private fun List<Feed>.hasLatestWithRepliesFeed(userId: String) = find { it.isLatestWithReplies(userId) } != null
}
