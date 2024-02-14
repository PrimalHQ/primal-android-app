package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentUserProfileStats(
    @SerialName("pubkey") val profileId: String,
    @SerialName("follows_count") val followsCount: Int? = null,
    @SerialName("followers_count") val followersCount: Int? = null,
    @SerialName("note_count") val noteCount: Int? = null,
    @SerialName("reply_count") val replyCount: Int? = null,
    @SerialName("relay_count") val relayCount: Int? = null,
    @SerialName("total_zap_count") val totalZapCount: Long? = null,
    @SerialName("total_satszapped") val totalSatsZapped: Long? = null,
    @SerialName("time_joined") val timeJoined: Long? = null,
)
