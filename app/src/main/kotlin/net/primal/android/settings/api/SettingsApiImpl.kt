package net.primal.android.settings.api

import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.settings.api.model.AppSettingsResponse
import javax.inject.Inject

class SettingsApiImpl @Inject constructor(
    private val socketClient: SocketClient,
) : SettingsApi {

    override suspend fun getDefaultAppSettings(): AppSettingsResponse {
        val result = socketClient.query(
            message = OutgoingMessage(
                primalVerb = "get_default_app_settings",
                options = null
            )
        )

        val settingsEvent = result.primalEvents.find {
            it.kind == NostrEventKind.PrimalDefaultSettings.value
        }

        return AppSettingsResponse(defaultSettings = settingsEvent)
    }

}
