package net.primal.android.notifications.api.model

import kotlinx.serialization.SerialName

data class NotificationsRequestBody(
    @SerialName("pubkey") val pubkey: String,
    @SerialName("user_pubkey") val userPubkey: String,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since") val since: Long? = null,
)
