package net.primal.android.settings.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.settings.api.SettingsApi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount

class SettingsRepository @Inject constructor(
    private val settingsApi: SettingsApi,
    private val database: PrimalDatabase,
    private val accountsStore: UserAccountsStore,
) {
    suspend fun fetchAndPersistAppSettings(userId: String) =
        withContext(Dispatchers.IO) {
            val appSettings = fetchAppSettings(userId = userId) ?: return@withContext
            persistAppSettings(userId = userId, appSettings = appSettings)
        }

    suspend fun updateAndPersistDefaultZapAmount(userId: String, defaultAmount: ULong) {
        updateAndPersistAppSettings(userId = userId) {
            copy(defaultZapAmount = defaultAmount)
        }
    }

    suspend fun updateAndPersistZapOptions(userId: String, zapOptions: List<ULong>) {
        updateAndPersistAppSettings(userId = userId) {
            copy(zapOptions = zapOptions)
        }
    }

    suspend fun updateAndPersistNotifications(userId: String, notifications: JsonObject) {
        updateAndPersistAppSettings(userId = userId) {
            copy(notifications = notifications)
        }
    }

    suspend fun addAndPersistUserFeed(
        userId: String,
        name: String,
        directive: String,
    ) {
        updateAndPersistAppSettings(userId = userId) {
            copy(
                feeds = feeds.toMutableList().apply {
                    add(ContentFeedData(name = name, directive = directive))
                },
            )
        }
    }

    suspend fun removeAndPersistUserFeed(userId: String, directive: String) {
        updateAndPersistAppSettings(userId = userId) {
            copy(
                feeds = feeds.toMutableList().apply {
                    removeAll { it.directive == directive }
                },
            )
        }
    }

    suspend fun updateAndPersistFeeds(userId: String, feeds: List<ContentFeedData>) {
        updateAndPersistAppSettings(userId = userId) {
            copy(feeds = feeds)
        }
    }

    suspend fun restoreDefaultFeeds(userId: String) {
        val remoteDefaultAppSettings = fetchDefaultAppSettings(userId = userId) ?: return
        updateAndPersistFeeds(userId = userId, feeds = remoteDefaultAppSettings.feeds)
    }

    private suspend fun updateAndPersistAppSettings(
        userId: String,
        reducer: ContentAppSettings.() -> ContentAppSettings,
    ) {
        val remoteAppSettings = fetchAppSettings(userId = userId) ?: return
        val newAppSettings = remoteAppSettings.reducer()
        settingsApi.setAppSettings(userId = userId, appSettings = newAppSettings)
        persistAppSettings(userId = userId, appSettings = newAppSettings)
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

    private suspend fun persistAppSettings(userId: String, appSettings: ContentAppSettings) {
        val currentUserAccount =
            accountsStore.findByIdOrNull(userId = userId) ?: UserAccount.buildLocal(pubkey = userId)

        val userFeeds = appSettings.feeds.distinctBy { it.directive }.map { it.asFeedPO() }
        val hasLatestFeed = userFeeds.find { it.directive == userId } != null
        val userIdDirectiveIndex = userFeeds.indexOfFirst { it.directive == userId }
        val finalFeeds = if (hasLatestFeed) {
            userFeeds.toMutableList().apply {
                if (userIdDirectiveIndex >= 0) {
                    removeAt(userIdDirectiveIndex)
                    add(userIdDirectiveIndex, Feed(directive = userId, name = "Latest"))
                }
            }
        } else {
            userFeeds.toMutableList().apply {
                add(0, Feed(directive = userId, name = "Latest"))
            }
        }

        accountsStore.upsertAccount(
            userAccount = currentUserAccount.copy(
                appSettings = appSettings.copy(feeds = finalFeeds.map { it.asContentFeedData() }),
            ),
        )

        database.withTransaction {
            database.feeds().deleteAll()
            database.feeds().upsertAll(data = finalFeeds)
        }
    }

    private fun ContentFeedData.asFeedPO(): Feed = Feed(name = name, directive = directive)
    private fun Feed.asContentFeedData(): ContentFeedData =
        ContentFeedData(
            name = name,
            directive = directive,
        )
}
