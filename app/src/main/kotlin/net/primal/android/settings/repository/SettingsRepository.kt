package net.primal.android.settings.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import net.primal.android.settings.api.SettingsApi
import net.primal.android.theme.active.ActiveThemeStore
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsApi: SettingsApi,
    private val database: PrimalDatabase,
    private val activeThemeStore: ActiveThemeStore,
) {

    suspend fun fetchAppSettings(pubkey: String) = withContext(Dispatchers.IO) {
        val response = settingsApi.getAppSettings(pubkey = pubkey)

        val appSettings = NostrJson.decodeFromStringOrNull<ContentAppSettings>(
            string = response.userSettings?.content ?: response.defaultSettings?.content
        ) ?: return@withContext

        database.withTransaction {
            val userFeeds = appSettings.feeds.map { it.asFeedPO() }
            val hasLatestFeed = userFeeds.find { it.directive == pubkey } != null
            val finalFeeds = if (hasLatestFeed) userFeeds else {
                userFeeds.toMutableList().apply {
                    add(0, Feed(directive = pubkey, name = "Latest"))
                }
            }
            database.feeds().deleteAll()
            database.feeds().upsertAll(data = finalFeeds)
        }

        if (appSettings.theme != null) {
            activeThemeStore.setUserTheme(appSettings.theme)
        }
    }

    private fun ContentFeedData.asFeedPO(): Feed = Feed(name = name, directive = directive)

}
