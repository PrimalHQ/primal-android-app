package net.primal.android.settings.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.GetMuteListResponse

interface SettingsApi {

    suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun getDefaultAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun setAppSettings(userId: String, appSettings: ContentAppSettings): NostrEvent

    suspend fun getMuteList(userId: String): GetMuteListResponse

    suspend fun setMuteList(userId: String, muteList: Set<String>): NostrEvent
}
