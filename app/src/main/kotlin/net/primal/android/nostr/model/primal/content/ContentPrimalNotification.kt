package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalNotification(
    val pubkey: String, // "pubkey": "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
    @SerialName("created_at") val createdAt: Long, // "created_at": 1690643558,
    val type: Int, // "type": 1,
    val follower: String? = null, //    (:follower, Nostr.PubKeyId),
    @SerialName("your_post") val yourPost: String? = null, //    (:your_post, Nostr.EventId),
    @SerialName("who_liked_it") val whoLikedIt: String? = null, //    (:who_liked_it, Nostr.PubKeyId),
    @SerialName("who_reposted_it") val whoRepostedIt: String? = null, //    (:who_reposted_it, Nostr.PubKeyId),
    @SerialName("who_zapped_it") val whoZappedIt: String? = null, //    (:who_zapped_it, Nostr.PubKeyId),
    @SerialName("satszapped") val satsZapped: Int? = null, //    (:satszapped, Int),
    @SerialName("who_replied_to_it") val whoRepliedToIt: String? = null, //    (:who_replied_to_it, Nostr.PubKeyId),
    val reply: String? = null, //    (:reply, Nostr.EventId),
    @SerialName("you_were_mentioned_by") val youWereMentionedBy: String? = null,
    @SerialName("you_were_mentioned_in") val youWereMentionedIn: String? = null, //    (:you_were_mentioned_in, Nostr.EventId),
    @SerialName("your_post_were_mentioned_in") val yourPostWereMentionedIn: String? = null, //    (:your_post_were_mentioned_in, Nostr.EventId),
    @SerialName("your_post_was_mentioned_by") val yourPostWasMentionedBy: String? = null,
    @SerialName("post_you_were_mentioned_in") val postYouWereMentionedIn: String? = null, //    (:post_you_were_mentioned_in, Nostr.EventId),
    @SerialName("post_your_post_was_mentioned_in") val postYourPostWasMentionedIn: String? = null, //    (:post_your_post_was_mentioned_in, Nostr.EventId),
)
