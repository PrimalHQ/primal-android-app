package net.primal.data.local.mapper

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.profiles.ProfileStats
import net.primal.data.remote.model.ContentProfilePremiumInfo
import net.primal.data.remote.model.ContentUserProfileStats
import net.primal.domain.PrimalEvent
import net.primal.domain.PrimalLegendProfile

fun List<PrimalEvent>.mapNotNullAsProfileStatsPO() = mapNotNull { it.asProfileStatsPO() }

fun List<PrimalEvent>.parseAndFoldPrimalUserNames() =
    map { it.parseAndMapPrimalUserNames() }.fold(emptyMap<String, String>()) { acc, curr -> acc + curr }

fun List<PrimalEvent>.parseAndFoldPrimalPremiumInfo() =
    map { it.parseAndMapPrimalPremiumInfo() }
        .fold(emptyMap<String, ContentProfilePremiumInfo>()) { acc, curr -> acc + curr }

fun List<PrimalEvent>.parseAndFoldPrimalLegendProfiles() =
    map { it.parseAndMapPrimalLegendProfiles() }
        .fold(emptyMap<String, PrimalLegendProfile>()) { acc, curr -> acc + curr }

fun PrimalEvent.asProfileStatsPO(): ProfileStats? {
    val content = takeContentAsUserProfileStatsOrNull() ?: return null
    return ProfileStats(
        profileId = content.profileId,
        joinedAt = content.timeJoined,
        following = content.followsCount,
        followers = content.followersCount,
        readsCount = content.readsCount,
        mediaCount = content.mediaCount,
        notesCount = content.noteCount,
        repliesCount = content.replyCount,
        relaysCount = content.relayCount,
        totalReceivedZaps = content.totalZapCount,
        contentZapCount = content.contentZapCount,
        totalReceivedSats = content.totalSatsZapped,
    )
}

private fun PrimalEvent.takeContentAsUserProfileStatsOrNull(): ContentUserProfileStats? {
    return this.content.decodeFromJsonStringOrNull<ContentUserProfileStats>()
}

fun PrimalEvent.takeContentAsPrimalUserScoresOrNull(): Map<String, Float> {
    return this.content.decodeFromJsonStringOrNull() ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalUserNames(): Map<String, String> {
    return this?.content.decodeFromJsonStringOrNull<Map<String, String>>() ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalLegendProfiles(): Map<String, PrimalLegendProfile> {
    return this?.content.decodeFromJsonStringOrNull<Map<String, PrimalLegendProfile>>() ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalPremiumInfo(): Map<String, ContentProfilePremiumInfo> {
    return this?.content.decodeFromJsonStringOrNull<Map<String, ContentProfilePremiumInfo>>() ?: emptyMap()
}

fun PrimalEvent.takeContentAsPrimalUserFollowersCountsOrNull(): Map<String, Int> {
    return this.content.decodeFromJsonStringOrNull<Map<String, Int>>() ?: emptyMap()
}

// fun PrimalEvent.takeContentAsPrimalUserFollowStats(): Map<String, NewUserFollowStats> {
//    return CommonJson.decodeFromString(this.content)
// }
