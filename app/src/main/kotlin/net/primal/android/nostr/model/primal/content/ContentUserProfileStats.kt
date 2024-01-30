package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentUserProfileStats(
    @SerialName("pubkey") val profileId: String,
    @SerialName("follows_count") val followsCount: Int,
    @SerialName("followers_count") val followersCount: Int,
    @SerialName("note_count") val noteCount: Int,
    @SerialName("reply_count") val replyCount: Int,
    @SerialName("time_joined") val timeJoined: Long,
    @SerialName("relay_count") val relayCount: Int,
    @SerialName("total_zap_count") val totalZapCount: Long,
    @SerialName("total_satszapped") val totalSatsZapped: Long,
)
