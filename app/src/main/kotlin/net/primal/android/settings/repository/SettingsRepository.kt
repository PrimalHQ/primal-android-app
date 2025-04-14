package net.primal.android.settings.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.api.settings.SettingsApi
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.notifications.DEFAULT_ZAP_CONFIG
import net.primal.domain.notifications.DEFAULT_ZAP_DEFAULT

class SettingsRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val settingsApi: SettingsApi,
    private val accountsStore: UserAccountsStore,
) {

    suspend fun fetchAndPersistAppSettings(authorizationEvent: NostrEvent) {
        val userId = authorizationEvent.pubKey
        val appSettings = fetchAppSettings(authorizationEvent) ?: return
        persistAppSettingsLocally(userId = userId, appSettings = appSettings)
    }

    suspend fun fetchAndUpdateAndPublishAppSettings(
        authorizationEvent: NostrEvent,
        signer: suspend ContentAppSettings.() -> NostrEvent,
    ) {
        val userId = authorizationEvent.pubKey
        withContext(dispatcherProvider.io()) {
            val remoteAppSettings = fetchAppSettings(authorizationEvent) ?: return@withContext
            persistAppSettingsLocally(userId = userId, appSettings = remoteAppSettings)
            updateAndPublishAppSettings(userId = userId, signer = signer)
        }
    }

    @Suppress("MagicNumber")
    suspend fun ensureZapConfig(authorizationEvent: NostrEvent, signer: suspend (ContentAppSettings) -> NostrEvent) {
        val userId = authorizationEvent.pubKey
        val userSettings = accountsStore.findByIdOrNull(userId)?.appSettings
        if (userSettings?.zapDefault != null && userSettings.zapsConfig.isNotEmpty()) return

        val defaultSettings = withContext(dispatcherProvider.io()) {
            fetchDefaultAppSettings(userId = userId)
        }
        val defaultZapDefault = defaultSettings?.zapDefault ?: DEFAULT_ZAP_DEFAULT
        val defaultZapsConfig = defaultSettings?.zapsConfig ?: DEFAULT_ZAP_CONFIG

        val existingZapDefaultValue = userSettings?.defaultZapAmount
        val existingZapsConfigValues = userSettings?.zapOptions

        fetchAndUpdateAndPublishAppSettings(authorizationEvent) {
            val newAppSettings = this.copy(
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
            signer(newAppSettings)
        }
    }

    private suspend fun updateAndPublishAppSettings(
        userId: String,
        signer: suspend ContentAppSettings.() -> NostrEvent,
    ) {
        withContext(dispatcherProvider.io()) {
            val localAppSettings = accountsStore.findByIdOrNull(userId = userId)?.appSettings ?: return@withContext
            val settingsEvent = localAppSettings.signer()
            publishAppSettings(settingsEvent)
        }
    }

    private suspend fun publishAppSettings(settingsEvent: NostrEvent) {
        val userId = settingsEvent.pubKey
        val newAppSettings = settingsEvent.getContentAppSettings()
        checkNotNull(newAppSettings) { "Invalid settings event. Unable to get app settings." }

        settingsApi.setAppSettings(settingsEvent)
        persistAppSettingsLocally(userId = userId, appSettings = newAppSettings)
    }

    private suspend fun fetchAppSettings(authorizationEvent: NostrEvent): ContentAppSettings? {
        val response = settingsApi.getAppSettings(authorizationEvent)
        return (response.userSettings?.content ?: response.defaultSettings?.content)
            .decodeFromJsonStringOrNull<ContentAppSettings>()
    }

    private suspend fun fetchDefaultAppSettings(userId: String): ContentAppSettings? {
        val response = settingsApi.getDefaultAppSettings(pubkey = userId)
        return response.defaultSettings?.content.decodeFromJsonStringOrNull<ContentAppSettings>()
    }

    private suspend fun persistAppSettingsLocally(userId: String, appSettings: ContentAppSettings) {
        val currentUserAccount = accountsStore.findByIdOrNull(userId = userId)
            ?: UserAccount.buildLocal(pubkey = userId)

        accountsStore.upsertAccount(userAccount = currentUserAccount.copy(appSettings = appSettings))
    }

    private fun NostrEvent.getContentAppSettings(): ContentAppSettings? {
        return this.content.decodeFromJsonStringOrNull<ContentAppSettings>()
    }
}
