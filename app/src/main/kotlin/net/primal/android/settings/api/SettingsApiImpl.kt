package net.primal.android.settings.api

import fr.acinq.secp256k1.Hex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.NostrUnsignedEvent
import net.primal.android.nostr.signOrThrow
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSettingsDescription
import net.primal.android.settings.api.model.AppSettingsRequest
import net.primal.android.settings.api.model.AppSettingsResponse
import net.primal.android.user.credentials.CredentialsStore
import java.time.Instant
import javax.inject.Inject

class SettingsApiImpl @Inject constructor(
    private val socketClient: SocketClient,
    private val credentialsStore: CredentialsStore,
) : SettingsApi {

    override suspend fun getAppSettings(pubkey: String): AppSettingsResponse {
        val credential = credentialsStore.findOrThrow(npub = Hex.decode(pubkey).toNpub())

        val unsignedEvent = NostrUnsignedEvent(
            pubKey = pubkey,
            createdAt = Instant.now().epochSecond,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf(buildJsonArray {
                add("d")
                add(UserAgentProvider.USER_AGENT)
            }),
            content = NostrJson.encodeToString(
                AppSettingsDescription(description = "Sync app settings")
            ),
        )
        val signedNostrEvent = unsignedEvent.signOrThrow(nsec = credential.nsec)

        val result = socketClient.query(
            message = OutgoingMessage(
                primalVerb = "get_app_settings",
                optionsJson = NostrJson.encodeToString(
                    AppSettingsRequest(
                        eventFromUser = signedNostrEvent,
                    )
                ),
            )
        )

        return AppSettingsResponse(
            userSettings = result.nostrEvents.find {
                it.kind == NostrEventKind.ApplicationSpecificData.value
            },
            defaultSettings = result.primalEvents.find {
                it.kind == NostrEventKind.PrimalDefaultSettings.value
            },
        )
    }

}
