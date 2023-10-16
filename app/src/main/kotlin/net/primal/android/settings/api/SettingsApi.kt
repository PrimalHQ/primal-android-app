package net.primal.android.settings.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.GetMutelistResponse

interface SettingsApi {

    suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse
    suspend fun getDefaultAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun setAppSettings(userId: String, appSettings: ContentAppSettings): NostrEvent

    suspend fun getMutelist(userId: String): GetMutelistResponse

    suspend fun setMutelist(userId: String, mutelist: Set<String>): NostrEvent
}