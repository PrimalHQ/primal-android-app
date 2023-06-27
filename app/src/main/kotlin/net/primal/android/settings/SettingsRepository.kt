package net.primal.android.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.serialization.NostrJson
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
        val appSettingsJsonContent = response.userSettings?.content
            ?: response.defaultSettings?.content
            ?: return@withContext

        val appSettings = NostrJson.decodeFromString<ContentAppSettings>(appSettingsJsonContent)

        database.feeds().upsertAll(
            data = listOf(
                Feed(directive = pubkey, name = "Latest, following"),
            ) + appSettings.feeds.map { it.asFeedPO() }
        )

        if (appSettings.theme != null) {
            activeThemeStore.setUserTheme(appSettings.theme)
        }
    }

    private fun ContentFeedData.asFeedPO(): Feed = Feed(name = name, directive = directive)

}
