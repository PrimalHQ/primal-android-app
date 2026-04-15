package net.primal.data.remote.api.feeds.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdvancedSearchQueryResponse(
    val includes: String = "",
    val excludes: String = "",
    val hashtags: String = "",
    val kind: String = "Notes",
    val postedBy: List<String> = emptyList(),
    @SerialName("replingTo") val replyingTo: List<String> = emptyList(),
    val zappedBy: List<String> = emptyList(),
    val timeframe: String = "Anytime",
    val customTimeframe: CustomTimeframe = CustomTimeframe(),
    val scope: String = "Global",
    val sortBy: String = "Time",
    val orientation: String = "Any",
    val minWords: Int = 0,
    val maxWords: Int = 0,
    val minDuration: Int = 0,
    val maxDuration: Int = 0,
    val minScore: Int = 0,
    val minInteractions: Int = 0,
    val minLikes: Int = 0,
    val minZaps: Int = 0,
    val minReplies: Int = 0,
    val minReposts: Int = 0,
    val following: List<String> = emptyList(),
    val userMentions: List<String> = emptyList(),
    val sentiment: String = "Neutral",
) {
    @Serializable
    data class CustomTimeframe(
        val since: String = "",
        val until: String = "",
    )
}
