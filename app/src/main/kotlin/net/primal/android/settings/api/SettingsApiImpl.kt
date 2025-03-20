package net.primal.android.settings.api

import javax.inject.Inject
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.api.model.AppSpecificDataRequest
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.GetMuteListRequest
import net.primal.android.settings.api.model.GetMuteListResponse
import net.primal.android.settings.api.model.SetAppSettingsRequest
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class SettingsApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : SettingsApi {

    override suspend fun getAppSettings(pubkey: String): GetAppSettingsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.GET_APP_SETTINGS.id,
                optionsJson = CommonJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAuthorizationNostrEvent(
                            userId = pubkey,
                            description = "Sync app settings",
                        ),
                    ),
                ),
            ),
        )

        return GetAppSettingsResponse(
            userSettings = queryResult.findNostrEvent(NostrEventKind.ApplicationSpecificData),
            defaultSettings = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultSettings),
        )
    }

    override suspend fun getDefaultAppSettings(pubkey: String): GetAppSettingsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = net.primal.data.remote.PrimalVerb.GET_DEFAULT_APP_SETTINGS.id),
        )

        return GetAppSettingsResponse(
            userSettings = null,
            defaultSettings = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultSettings),
        )
    }

    override suspend fun setAppSettings(userId: String, appSettings: ContentAppSettings): NostrEvent {
        val signedNostrEvent = nostrNotary.signAppSettingsNostrEvent(
            userId = userId,
            appSettings = appSettings,
        )

        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.SET_APP_SETTINGS.id,
                optionsJson = CommonJson.encodeToString(
                    SetAppSettingsRequest(settingsEvent = signedNostrEvent),
                ),
            ),
        )

        return signedNostrEvent
    }

    override suspend fun getMuteList(userId: String): GetMuteListResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MUTE_LIST.id,
                optionsJson = CommonJson.encodeToString(
                    GetMuteListRequest(pubkey = userId),
                ),
            ),
        )

        return GetMuteListResponse(
            muteList = queryResult.findNostrEvent(NostrEventKind.MuteList),
            metadataEvents = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }
}
