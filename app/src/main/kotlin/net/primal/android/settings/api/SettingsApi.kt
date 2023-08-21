package net.primal.android.settings.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.settings.api.model.GetAppSettingsResponse

interface SettingsApi {

    suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun setAppSettings(userId: String, appSettings: ContentAppSettings): NostrEvent
}