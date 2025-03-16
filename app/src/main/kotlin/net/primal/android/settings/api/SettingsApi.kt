package net.primal.android.settings.api

import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.GetMuteListResponse
import net.primal.domain.nostr.NostrEvent

interface SettingsApi {

    suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun getDefaultAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun setAppSettings(userId: String, appSettings: ContentAppSettings): NostrEvent

    suspend fun getMuteList(userId: String): GetMuteListResponse
}
