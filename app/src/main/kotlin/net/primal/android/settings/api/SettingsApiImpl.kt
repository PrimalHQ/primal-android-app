package net.primal.android.settings.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.GetAppSettingsRequest
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.SetAppSettingsRequest
import javax.inject.Inject

class SettingsApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : SettingsApi {

    override suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_APP_SETTINGS,
                optionsJson = NostrJson.encodeToString(
                    GetAppSettingsRequest(
                        eventFromUser = nostrNotary.signAppSettingsSyncNostrEvent(pubkey),
                    )
                ),
            )
        )

        return GetAppSettingsResponse(
            userSettings = queryResult.findNostrEvent(NostrEventKind.ApplicationSpecificData),
            defaultSettings = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultSettings),
        )
    }

    override suspend fun setAppSettings(
        userId: String,
        appSettings: ContentAppSettings
    ): NostrEvent {
        val signedNostrEvent = nostrNotary.signAppSettingsNostrEvent(
            userId = userId,
            appSettings = appSettings,
        )

        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_APP_SETTINGS,
                optionsJson = NostrJson.encodeToString(
                    SetAppSettingsRequest(settingsEvent = signedNostrEvent)
                ),
            )
        )

        return signedNostrEvent
    }
}
