package net.primal.android.settings.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSpecificDataRequest
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.GetMutelistRequest
import net.primal.android.settings.api.model.GetMutelistResponse
import net.primal.android.settings.api.model.SetAppSettingsRequest
import javax.inject.Inject

class SettingsApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val relaysManager: RelaysManager,
    private val nostrNotary: NostrNotary,
) : SettingsApi {

    override suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse {
        val queryResult = primalApiClient.query(
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

    override suspend fun getMutelist(userId: String): GetMutelistResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MUTELIST,
                optionsJson = NostrJson.encodeToString(
                    GetMutelistRequest(pubkey = userId)
                ),
            )
        )

        return GetMutelistResponse(mutelist = queryResult.findNostrEvent(NostrEventKind.MuteList))
    }

    override suspend fun setMutelist(userId: String, mutelist: Set<String>): NostrEvent {
        val signedNostrEvent = nostrNotary.signMutelistNostrEvent(
            userId = userId,
            pubkeys = mutelist,
        )

        relaysManager.publishEvent(signedNostrEvent)

        return signedNostrEvent
    }
}
