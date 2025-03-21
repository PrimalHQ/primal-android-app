package net.primal.data.remote.api.settings

import net.primal.data.remote.api.settings.model.GetAppSettingsResponse
import net.primal.data.remote.api.settings.model.GetMuteListResponse
import net.primal.domain.nostr.NostrEvent

interface SettingsApi {

    suspend fun getAppSettings(authorization: NostrEvent): GetAppSettingsResponse

    suspend fun getDefaultAppSettings(pubkey: String): GetAppSettingsResponse

    suspend fun setAppSettings(settingsEvent: NostrEvent)

    suspend fun getMuteList(userId: String): GetMuteListResponse
}
