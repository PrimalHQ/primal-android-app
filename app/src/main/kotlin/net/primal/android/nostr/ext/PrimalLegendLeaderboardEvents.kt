package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalLeaderboardItem
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.premium.legend.model.LegendLeaderboardEntry
import net.primal.android.profile.db.ProfileData


fun PrimalEvent?.parseAndMapAsLeaderboardEntries(profiles: Map<String, ProfileData>) =
    NostrJson.decodeFromStringOrNull<List<ContentPrimalLeaderboardItem>>(this?.content)
        ?.mapNotNull { item ->
            profiles[item.pubkey]?.let { profile ->
                LegendLeaderboardEntry(
                    userId = item.pubkey,
                    avatarCdnImage = profile.avatarCdnImage,
                    displayName = profile.authorNameUiFriendly(),
                    internetIdentifier = profile.internetIdentifier,
                    legendaryCustomization = profile.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
                    donatedBtc = item.donatedBtc,
                )
            }
        } ?: emptyList()
