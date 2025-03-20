package net.primal.android.settings.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.model.primal.content.ContentZapConfigItem
import net.primal.android.nostr.model.primal.content.ContentZapDefault
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_CONFIG
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_DEFAULT
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.settings.api.SettingsApi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import timber.log.Timber

class SettingsRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val settingsApi: SettingsApi,
    private val accountsStore: UserAccountsStore,
) {

    suspend fun fetchAndPersistAppSettings(userId: String) {
        val appSettings = fetchAppSettings(userId = userId) ?: return
        persistAppSettingsLocally(userId = userId, appSettings = appSettings)
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

    private suspend fun fetchAppSettings(userId: String): ContentAppSettings? =
        try {
            val response = settingsApi.getAppSettings(pubkey = userId)
            CommonJson.decodeFromStringOrNull<ContentAppSettings>(
                string = response.userSettings?.content ?: response.defaultSettings?.content,
            )
        } catch (error: MissingPrivateKeyException) {
            Timber.w(error)
            null
        }

    private suspend fun fetchDefaultAppSettings(userId: String): ContentAppSettings? {
        val response = settingsApi.getDefaultAppSettings(pubkey = userId)
        return CommonJson.decodeFromStringOrNull<ContentAppSettings>(
            string = response.defaultSettings?.content,
        )
    }

    private suspend fun persistAppSettingsLocally(userId: String, appSettings: ContentAppSettings) {
        val currentUserAccount = accountsStore.findByIdOrNull(userId = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(userAccount = currentUserAccount.copy(appSettings = appSettings))
    }
}
