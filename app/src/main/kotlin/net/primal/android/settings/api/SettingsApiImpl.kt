package net.primal.android.settings.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSettingsRequest
import net.primal.android.settings.api.model.AppSettingsResponse
import javax.inject.Inject

class SettingsApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : SettingsApi {

    override suspend fun getAppSettings(pubkey: String): AppSettingsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = "get_app_settings",
                optionsJson = NostrJson.encodeToString(
                    AppSettingsRequest(
                        eventFromUser = nostrNotary.signAppSettingsSyncNostrEvent(pubkey),
                    )
                ),
            )
        )

        return AppSettingsResponse(
            userSettings = queryResult.findNostrEvent(NostrEventKind.ApplicationSpecificData),
            defaultSettings = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultSettings),
        )
    }

}
