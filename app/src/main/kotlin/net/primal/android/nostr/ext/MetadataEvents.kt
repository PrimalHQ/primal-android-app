package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.attachments.domain.CdnResource
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.domain.PrimalLegendProfile
import net.primal.android.wallet.api.decodeLNUrlOrNull
import net.primal.android.wallet.api.parseAsLNUrlOrNull

fun List<NostrEvent>.mapAsProfileDataPO(
    cdnResources: List<CdnResource>,
    primalUserNames: Map<String, String>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
) = map { nostrEvent ->
    nostrEvent.asProfileDataPO(
        cdnResources = cdnResources.asMapByKey { it.url },
        primalUserNames = primalUserNames,
        primalLegendProfiles = primalLegendProfiles,

    )
}

fun List<NostrEvent>.mapAsProfileDataPO(
    cdnResources: Map<String, CdnResource>,
    primalUserNames: Map<String, String>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
) = map {
    it.asProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalLegendProfiles = primalLegendProfiles,
    )
}

fun NostrEvent.asProfileDataPO(
    cdnResources: Map<String, CdnResource>,
    primalUserNames: Map<String, String>,
    primalLegendProfiles: Map<String, PrimalLegendProfile>,
): ProfileData {
    val metadata = NostrJson.decodeFromStringOrNull<ContentMetadata>(this.content)

    return ProfileData(
        eventId = this.id,
        ownerId = this.pubKey,
        createdAt = this.createdAt,
        raw = NostrJson.encodeToString(this.toJsonObject()),
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
        primalName = primalUserNames[this.pubKey],
        primalLegendProfile = primalLegendProfiles[this.pubKey],
    )
}
