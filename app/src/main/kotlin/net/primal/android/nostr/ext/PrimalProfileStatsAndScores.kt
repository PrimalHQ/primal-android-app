package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.explore.api.model.NewUserFollowStats
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentUserProfileStats
import net.primal.android.profile.db.ProfileStats

fun List<PrimalEvent>.mapNotNullAsProfileStatsPO() = mapNotNull { it.asProfileStatsPO() }

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
    return NostrJson.decodeFromStringOrNull<ContentUserProfileStats>(this.content)
}

fun PrimalEvent.takeContentAsPrimalUserScoresOrNull(): Map<String, Float> {
    return NostrJson.decodeFromString(this.content)
}

fun List<PrimalEvent>.parseAndMapPrimalUserNames() =
    this.map {
        runCatching {
            it.takeContentAsPrimalUserNameOrNull()
        }.getOrNull()
    }.filterNotNull().fold(emptyMap<String, String>()) { acc, item -> acc + item }

fun PrimalEvent?.parseAndMapPrimalUserName() =
    listOf(this).filterNotNull().parseAndMapPrimalUserNames()

fun PrimalEvent.takeContentAsPrimalUserNameOrNull(): Map<String, String> {
    return NostrJson.decodeFromString(this.content)
}

fun PrimalEvent.takeContentAsPrimalUserFollowersCountsOrNull(): Map<String, Int> {
    return NostrJson.decodeFromString(this.content)
}

fun PrimalEvent.takeContentAsPrimalUserFollowStats(): Map<String, NewUserFollowStats> {
    return NostrJson.decodeFromString(this.content)
}
