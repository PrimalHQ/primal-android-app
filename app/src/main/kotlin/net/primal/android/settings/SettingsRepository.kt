package net.primal.android.settings

import net.primal.android.nostr.primal.PrimalApi
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val primalApi: PrimalApi,
) {

    val defaultFeed: String = "9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a"

    fun fetchDefaultAppSettingsToDatabase() {
        primalApi.requestDefaultAppSettings()
    }

}

