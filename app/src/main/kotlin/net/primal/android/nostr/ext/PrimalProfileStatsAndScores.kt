package net.primal.android.nostr.ext

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentUserProfileStats
import net.primal.android.profile.db.ProfileStats

fun List<PrimalEvent>.mapNotNullAsProfileStatsPO() =
    this.mapNotNull {
        NostrJson.decodeFromStringOrNull<ContentUserProfileStats>(it.content)
    }.map { it.asProfileStats() }

fun PrimalEvent.takeContentAsUserProfileStatsOrNull(): ContentUserProfileStats? {
    return try {
        NostrJson.decodeFromJsonElement<ContentUserProfileStats>(
            NostrJson.parseToJsonElement(this.content),
        )
    } catch (error: IllegalArgumentException) {
        null
    }
}

fun ContentUserProfileStats.asProfileStats() =
    ProfileStats(
        profileId = this.profileId,
        following = this.followsCount,
        followers = this.followersCount,
        notes = this.noteCount,
    )

fun PrimalEvent.takeContentAsPrimalUserScoresOrNull(): Map<String, Float> {
    return NostrJson.decodeFromString(this.content)
}
