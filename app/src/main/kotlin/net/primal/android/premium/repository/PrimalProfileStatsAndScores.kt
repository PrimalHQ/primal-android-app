package net.primal.android.premium.repository

import net.primal.android.nostr.model.primal.content.ContentProfilePremiumInfo
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.PrimalEvent
import net.primal.domain.PrimalLegendProfile

fun List<PrimalEvent>.parseAndFoldPrimalUserNames() =
    map { it.parseAndMapPrimalUserNames() }.fold(emptyMap<String, String>()) { acc, curr -> acc + curr }

// fun List<PrimalEvent>.parseAndFoldPrimalPremiumInfo() =
//    map { it.parseAndMapPrimalPremiumInfo() }
//        .fold(emptyMap<String, ContentProfilePremiumInfo>()) { acc, curr -> acc + curr }

fun List<PrimalEvent>.parseAndFoldPrimalLegendProfiles() =
    map { it.parseAndMapPrimalLegendProfiles() }
        .fold(emptyMap<String, PrimalLegendProfile>()) { acc, curr -> acc + curr }

fun PrimalEvent?.parseAndMapPrimalUserNames(): Map<String, String> {
    return CommonJson.decodeFromStringOrNull<Map<String, String>>(this?.content) ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalLegendProfiles(): Map<String, PrimalLegendProfile> {
    return CommonJson.decodeFromStringOrNull<Map<String, PrimalLegendProfile>>(this?.content) ?: emptyMap()
}

fun PrimalEvent?.parseAndMapPrimalPremiumInfo(): Map<String, ContentProfilePremiumInfo> {
    return CommonJson.decodeFromStringOrNull<Map<String, ContentProfilePremiumInfo>>(this?.content) ?: emptyMap()
}
