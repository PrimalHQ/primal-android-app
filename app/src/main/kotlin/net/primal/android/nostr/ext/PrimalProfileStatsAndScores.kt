package net.primal.android.nostr.ext

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentUserProfileStats
import net.primal.android.profile.db.ProfileStats
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull

fun List<PrimalEvent>.mapNotNullAsProfileStatsPO() =
    this.mapNotNull {
        NostrJson.decodeFromStringOrNull<ContentUserProfileStats>(it.content)
    }.map { it.asProfileStats() }

fun PrimalEvent.takeContentAsUserProfileStatsOrNull(): ContentUserProfileStats? {
    return try {
        NostrJson.decodeFromJsonElement<ContentUserProfileStats>(
            NostrJson.parseToJsonElement(this.content)
        )
    } catch (error: IllegalArgumentException) {
        null
    }
}

// TODO Update after backend confirmation
fun ContentUserProfileStats.asProfileStats() = ProfileStats(
    profileId = this.profileId ?: throw RuntimeException(),
    following = this.followsCount,
    followers = this.followersCount,
    notes = this.noteCount,
)

fun ContentUserProfileStats.asProfileStats(profileId: String) = ProfileStats(
    profileId = profileId,
    following = this.followsCount,
    followers = this.followersCount,
    notes = this.noteCount,
)

fun PrimalEvent.takeContentAsPrimalUserScoresOrNull(): Map<String, Float> {
    return NostrJson.decodeFromString(this.content)
}
