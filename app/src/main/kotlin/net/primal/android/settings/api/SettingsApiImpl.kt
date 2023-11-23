package net.primal.android.settings.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.api.model.AppSpecificDataRequest
import net.primal.android.settings.api.model.GetAppSettingsResponse
import net.primal.android.settings.api.model.GetMuteListRequest
import net.primal.android.settings.api.model.GetMuteListResponse
import net.primal.android.settings.api.model.SetAppSettingsRequest

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
            message = PrimalCacheFilter(primalVerb = PrimalVerb.GET_DEFAULT_APP_SETTINGS),
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
                primalVerb = PrimalVerb.SET_APP_SETTINGS,
                optionsJson = NostrJson.encodeToString(
                    SetAppSettingsRequest(settingsEvent = signedNostrEvent),
                ),
            ),
        )

        return signedNostrEvent
    }

    override suspend fun getMuteList(userId: String): GetMuteListResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MUTE_LIST,
                optionsJson = NostrJson.encodeToString(
                    GetMuteListRequest(pubkey = userId),
                ),
            ),
        )

        return GetMuteListResponse(
            muteList = queryResult.findNostrEvent(NostrEventKind.MuteList),
            metadataEvents = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
        )
    }

    override suspend fun setMuteList(userId: String, muteList: Set<String>): NostrEvent {
        val signedNostrEvent = nostrNotary.signMuteListNostrEvent(
            userId = userId,
            mutedUserIds = muteList,
        )

        relaysManager.publishEvent(signedNostrEvent)

        return signedNostrEvent
    }
}
