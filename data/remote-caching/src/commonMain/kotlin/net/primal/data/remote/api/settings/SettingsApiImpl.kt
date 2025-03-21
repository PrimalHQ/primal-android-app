package net.primal.data.remote.api.settings

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.settings.model.GetAppSettingsResponse
import net.primal.data.remote.api.settings.model.GetMuteListRequest
import net.primal.data.remote.api.settings.model.GetMuteListResponse
import net.primal.data.remote.api.settings.model.SetAppSettingsRequest
import net.primal.data.remote.model.AppSpecificDataRequest
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class SettingsApiImpl(
    private val primalApiClient: PrimalApiClient,
) : SettingsApi {

    override suspend fun getAppSettings(authorization: NostrEvent): GetAppSettingsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_APP_SETTINGS.id,
                optionsJson = AppSpecificDataRequest(eventFromUser = authorization).encodeToJsonString(),
            ),
        )

        return GetAppSettingsResponse(
            userSettings = queryResult.findNostrEvent(NostrEventKind.ApplicationSpecificData),
            defaultSettings = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultSettings),
        )
    }

    override suspend fun getDefaultAppSettings(pubkey: String): GetAppSettingsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = PrimalVerb.GET_DEFAULT_APP_SETTINGS.id),
        )

        return GetAppSettingsResponse(
            userSettings = null,
            defaultSettings = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultSettings),
        )
    }

    override suspend fun setAppSettings(settingsEvent: NostrEvent) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.SET_APP_SETTINGS.id,
                optionsJson = CommonJson.encodeToString(
                    SetAppSettingsRequest(settingsEvent = settingsEvent),
                ),
            ),
        )
    }

    override suspend fun getMuteList(userId: String): GetMuteListResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MUTE_LIST.id,
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
