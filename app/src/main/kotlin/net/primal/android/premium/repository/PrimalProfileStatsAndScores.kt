package net.primal.android.premium.repository

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.ContentProfilePremiumInfo
import net.primal.domain.PrimalEvent
import net.primal.domain.PrimalLegendProfile

fun List<PrimalEvent>.parseAndFoldPrimalUserNames() =
    map { it.parseAndMapPrimalUserNames() }.fold(emptyMap<String, String>()) { acc, curr -> acc + curr }

fun List<PrimalEvent>.parseAndFoldPrimalPremiumInfo() =
    map { it.parseAndMapPrimalPremiumInfo() }
        .fold(emptyMap<String, ContentProfilePremiumInfo>()) { acc, curr -> acc + curr }

fun List<PrimalEvent>.parseAndFoldPrimalLegendProfiles() =
    map { it.parseAndMapPrimalLegendProfiles() }
        .fold(emptyMap<String, PrimalLegendProfile>()) { acc, curr -> acc + curr }

fun PrimalEvent?.parseAndMapPrimalUserNames(): Map<String, String> {
    return this?.content.decodeFromJsonStringOrNull<Map<String, String>>() ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalLegendProfiles(): Map<String, PrimalLegendProfile> {
    return this?.content.decodeFromJsonStringOrNull<Map<String, PrimalLegendProfile>>() ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalPremiumInfo(): Map<String, ContentProfilePremiumInfo> {
    return this?.content.decodeFromJsonStringOrNull<Map<String, ContentProfilePremiumInfo>>() ?: emptyMap()
}
