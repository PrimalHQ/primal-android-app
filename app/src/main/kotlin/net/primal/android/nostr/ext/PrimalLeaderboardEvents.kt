package net.primal.android.nostr.ext

import net.primal.android.core.compose.profile.model.asPremiumProfileDataUi
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentLegendLeaderboardItem
import net.primal.android.nostr.model.primal.content.ContentPremiumLeaderboardItem
import net.primal.android.premium.leaderboard.domain.LeaderboardLegendEntry
import net.primal.android.premium.leaderboard.domain.OGLeaderboardEntry
import net.primal.android.profile.db.ProfileData

fun PrimalEvent?.parseAndMapAsLeaderboardLegendEntries(profiles: Map<String, ProfileData>) =
    NostrJson.decodeFromStringOrNull<List<ContentLegendLeaderboardItem>>(this?.content)
        ?.mapNotNull { item ->
            profiles[item.pubkey]?.let { profile ->
                LeaderboardLegendEntry(
                    userId = item.pubkey,
                    avatarCdnImage = profile.avatarCdnImage,
                    displayName = profile.authorNameUiFriendly(),
                    internetIdentifier = profile.internetIdentifier,
                    premiumProfileDataUi = profile.primalPremiumInfo?.asPremiumProfileDataUi(),
                    donatedBtc = item.donatedBtc,
                )
            }
        } ?: emptyList()

fun PrimalEvent?.parseAndMapAsOGLeaderboardEntries(profiles: Map<String, ProfileData>) =
    NostrJson.decodeFromStringOrNull<List<ContentPremiumLeaderboardItem>>(this?.content)
        ?.mapNotNull { item ->
            profiles[item.pubkey]?.let { profile ->
                OGLeaderboardEntry(
                    index = item.index,
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
