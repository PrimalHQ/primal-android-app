package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalNotification(
    val pubkey: String,
    @SerialName("created_at") val createdAt: Long,
    val type: Int,
    val follower: String? = null,
    @SerialName("your_post") val yourPost: String? = null,
    @SerialName("who_liked_it") val whoLikedIt: String? = null,
    @SerialName("who_reposted_it") val whoRepostedIt: String? = null,
    @SerialName("who_zapped_it") val whoZappedIt: String? = null,
    @SerialName("satszapped") val satsZapped: Long? = null,
    @SerialName("who_replied_to_it") val whoRepliedToIt: String? = null,
    val reply: String? = null,
    @SerialName("you_were_mentioned_by") val youWereMentionedBy: String? = null,
    @SerialName("you_were_mentioned_in") val youWereMentionedIn: String? = null,
    @SerialName("your_post_were_mentioned_in") val yourPostWereMentionedIn: String? = null,
    @SerialName("your_post_was_mentioned_by") val yourPostWasMentionedBy: String? = null,
    @SerialName("post_you_were_mentioned_in") val postYouWereMentionedIn: String? = null,
    @SerialName("post_your_post_was_mentioned_in") val postYourPostWasMentionedIn: String? = null,
)
