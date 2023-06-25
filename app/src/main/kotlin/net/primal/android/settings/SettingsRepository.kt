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
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsApi: SettingsApi,
    private val database: PrimalDatabase,
) {

    suspend fun fetchDefaultAppSettingsToDatabase(pubkey: String) = withContext(Dispatchers.IO) {
        settingsApi.getDefaultAppSettings().defaultSettings?.let { primalEvent ->
            database.feeds().upsertAll(
                data = listOf(
                    Feed(directive = pubkey, name = "Latest, following"),
                    Feed(directive = "network;trending", name = "Trending, my network"),
                ) + listOf(primalEvent)
                    .map { NostrJson.decodeFromString<ContentAppSettings>(it.content) }
                    .flatMap { it.feeds }
                    .map { it.asFeedPO() }
            )
        }
    }

    private fun ContentFeedData.asFeedPO(): Feed = Feed(name = name, directive = directive)

}
