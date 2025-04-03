package net.primal.android.premium.repository

import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.nostr.model.primal.content.ContentLegendLeaderboardItem
import net.primal.android.nostr.model.primal.content.ContentPremiumLeaderboardItem
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.LeaderboardLegendEntry
import net.primal.domain.OGLeaderboardEntry
import net.primal.domain.PrimalEvent
import net.primal.domain.model.ProfileData

fun PrimalEvent?.parseAndMapAsLeaderboardLegendEntries(profiles: Map<String, ProfileData>) =
    this?.content?.decodeFromJsonStringOrNull<List<ContentLegendLeaderboardItem>>()
        ?.mapNotNull { item ->
            profiles[item.pubkey]?.let { profile ->
                LeaderboardLegendEntry(
                    userId = item.pubkey,
                    avatarCdnImage = profile.avatarCdnImage,
                    displayName = profile.authorNameUiFriendly(),
                    internetIdentifier = profile.internetIdentifier,
                    legendSince = profile.primalPremiumInfo?.legendSince,
                    primalLegendProfile = profile.primalPremiumInfo?.legendProfile,
                    donatedSats = item.donatedSats.toULong(),
                )
            }
        } ?: emptyList()

fun PrimalEvent?.parseAndMapAsOGLeaderboardEntries(profiles: Map<String, ProfileData>) =
    this?.content?.decodeFromJsonStringOrNull<List<ContentPremiumLeaderboardItem>>()
        ?.mapNotNull { item ->
            profiles[item.pubkey]?.let { profile ->
                OGLeaderboardEntry(
                    index = item.index.toInt(),
                    userId = item.pubkey,
                    avatarCdnImage = profile.avatarCdnImage,
                    displayName = profile.authorNameUiFriendly(),
                    internetIdentifier = profile.internetIdentifier,
                    firstCohort = profile.primalPremiumInfo?.cohort1,
                    secondCohort = profile.primalPremiumInfo?.cohort2,
                    premiumSince = item.premiumSince,
                )
            }
        } ?: emptyList()
