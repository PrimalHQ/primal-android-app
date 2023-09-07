package net.primal.android.settings.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSpecificDataRequest
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.SetAppSettingsRequest
import javax.inject.Inject
import javax.inject.Named

class SettingsApiImpl @Inject constructor(
    @Named("Api") private val primalClient: PrimalClient,
    private val nostrNotary: NostrNotary,
) : SettingsApi {

    override suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse {
        val queryResult = primalClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_APP_SETTINGS,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSettingsSyncNostrEvent(
                            userId = pubkey,
                            description = "Sync app settings",
                        ),
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

        primalClient.query(
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
