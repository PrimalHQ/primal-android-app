package net.primal.android.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.processor.PrimalSettingsProcessor
import net.primal.android.settings.api.SettingsApi
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsApi: SettingsApi,
    private val database: PrimalDatabase,
) {

    val defaultFeed: String = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a"

    suspend fun fetchDefaultAppSettingsToDatabase() = withContext(Dispatchers.IO) {
        val response = settingsApi.getDefaultAppSettings()
        if (response.defaultSettings != null) {
            val settingsProcessor = PrimalSettingsProcessor(database = database)
            settingsProcessor.process(listOf(response.defaultSettings))
        }
    }

}
