package net.primal.android.settings.api

import net.primal.android.settings.api.model.AppSettingsResponse

interface SettingsApi {

    suspend fun getAppSettings(pubkey: String): AppSettingsResponse

}