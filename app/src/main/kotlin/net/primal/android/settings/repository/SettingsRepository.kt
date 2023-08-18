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
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsApi: SettingsApi,
    private val database: PrimalDatabase,
    private val activeThemeStore: ActiveThemeStore,
    private val accountsStore: UserAccountsStore,
) {

    suspend fun fetchAndPersistAppSettings(userId: String) = withContext(Dispatchers.IO) {
        val appSettings = fetchAppSettings(userId = userId) ?: return@withContext
        persistAppSettings(userId = userId, appSettings = appSettings)
    }

    suspend fun updateAndPersistDefaultZapAmount(userId: String, defaultAmount: Long) {
        updateAndPersistAppSettings(userId = userId) {
            copy(defaultZapAmount = defaultAmount)
        }
    }

    suspend fun updateAndPersistZapOptions(userId: String, zapOptions: List<Long>) {
        updateAndPersistAppSettings(userId = userId) {
            copy(zapOptions = zapOptions)
        }
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
            string = response.userSettings?.content ?: response.defaultSettings?.content
        )
    }

    private suspend fun persistAppSettings(userId: String, appSettings: ContentAppSettings) {
        val currentUserAccount = accountsStore.findByIdOrNull(pubkey = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(
            userAccount = currentUserAccount.copy(
                appSettings = appSettings,
            )
        )

        database.withTransaction {
            val userFeeds = appSettings.feeds.map { it.asFeedPO() }
            val hasLatestFeed = userFeeds.find { it.directive == userId } != null
            val finalFeeds = if (hasLatestFeed) userFeeds else {
                userFeeds.toMutableList().apply {
                    add(0, Feed(directive = userId, name = "Latest"))
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
