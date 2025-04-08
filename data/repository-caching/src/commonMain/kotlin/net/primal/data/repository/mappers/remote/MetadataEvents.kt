package net.primal.data.repository.mappers.remote

import net.primal.core.utils.asMapByKey
import net.primal.core.utils.parseUris
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.domain.CdnImage
import net.primal.domain.CdnResource
import net.primal.domain.ContentProfilePremiumInfo
import net.primal.domain.PrimalLegendProfile
import net.primal.domain.PrimalPremiumInfo
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.decodeLNUrlOrNull
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull
import net.primal.domain.nostr.utils.parseHashtags

fun List<NostrEvent>.mapAsProfileDataPO(
    cdnResources: List<CdnResource>,
    primalUserNames: Map<String, String>,
    primalPremiumInfo: Map<String, ContentProfilePremiumInfo>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
    blossomServers: Map<String, List<String>>,
): List<ProfileData> {
    val cdnResourcesMapByUrl = cdnResources.asMapByKey { it.url }
    return this.map { nostrEvent ->
        nostrEvent.asProfileDataPO(
            cdnResources = cdnResourcesMapByUrl,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = primalLegendProfiles,
            blossomServers = blossomServers,
        )
    }
}

fun List<NostrEvent>.mapAsProfileDataPO(
    cdnResourcesMap: Map<String, CdnResource>,
    primalUserNames: Map<String, String>,
    primalPremiumInfo: Map<String, ContentProfilePremiumInfo>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
    blossomServers: Map<String, List<String>>,
): List<ProfileData> =
    map {
        it.asProfileDataPO(
            cdnResources = cdnResourcesMap,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = primalLegendProfiles,
            blossomServers = blossomServers,
        )
    }

fun NostrEvent.asProfileDataPO(
    cdnResources: List<CdnResource>,
    primalUserNames: Map<String, String>,
    primalPremiumInfo: Map<String, ContentProfilePremiumInfo>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
    blossomServers: Map<String, List<String>>,
) = asProfileDataPO(
    cdnResources = cdnResources.asMapByKey { it.url },
    primalUserNames = primalUserNames,
    primalPremiumInfo = primalPremiumInfo,
    primalLegendProfiles = primalLegendProfiles,
    blossomServers = blossomServers,
)

fun NostrEvent.asProfileDataPO(
    cdnResources: Map<String, CdnResource>,
    primalUserNames: Map<String, String>,
    primalPremiumInfo: Map<String, ContentProfilePremiumInfo>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
    blossomServers: Map<String, List<String>>,
): ProfileData {
    val metadata = this.content.decodeFromJsonStringOrNull<ContentMetadata>()
    val premiumInfo = primalPremiumInfo[this.pubKey]
    val blossoms = blossomServers[this.pubKey]
    return ProfileData(
        eventId = this.id,
        ownerId = this.pubKey,
        createdAt = this.createdAt,
        raw = this.toNostrJsonObject().encodeToJsonString(),
        handle = metadata?.name,
        internetIdentifier = metadata?.nip05,
        lightningAddress = metadata?.lud16,
        lnUrlDecoded = metadata?.lud16?.parseAsLNUrlOrNull() ?: metadata?.lud06?.decodeLNUrlOrNull(),
        about = metadata?.about,
        aboutUris = metadata?.about?.parseUris() ?: emptyList(),
        aboutHashtags = metadata?.about?.parseHashtags() ?: emptyList(),
        displayName = metadata?.displayName,
        avatarCdnImage = metadata?.picture?.let {
            CdnImage(
                sourceUrl = it,
                variants = cdnResources[it]?.variants ?: emptyList(),
            )
        },
        bannerCdnImage = metadata?.banner?.let {
            CdnImage(
                sourceUrl = it,
                variants = cdnResources[it]?.variants ?: emptyList(),
            )
        },
        website = metadata?.website,
        primalPremiumInfo = PrimalPremiumInfo(
            primalName = primalUserNames[this.pubKey],
            cohort1 = premiumInfo?.cohort1,
            cohort2 = premiumInfo?.cohort2,
            tier = premiumInfo?.tier,
            expiresAt = premiumInfo?.expiresAt,
            legendSince = premiumInfo?.legendSince,
            premiumSince = premiumInfo?.premiumSince,
            legendProfile = primalLegendProfiles[this.pubKey],
        ),
        blossoms = blossoms ?: emptyList(),
    )
}
