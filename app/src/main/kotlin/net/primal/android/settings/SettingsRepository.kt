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

    val defaultFeed: String = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a"

    suspend fun fetchDefaultAppSettingsToDatabase() = withContext(Dispatchers.IO) {
        settingsApi.getDefaultAppSettings().defaultSettings?.let { primalEvent ->
            database.feeds().upsertAll(
                data = listOf(primalEvent)
                    .map { NostrJson.decodeFromString<ContentAppSettings>(it.content) }
                    .flatMap { it.feeds }
                    .map { it.asFeedPO() }
            )
        }
    }

    private fun ContentFeedData.asFeedPO(): Feed = Feed(name = name, directive = directive)

}
