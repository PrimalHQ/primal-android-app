package net.primal.android.core.push.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateNotificationTokenRequest(
    @SerialName("pubkeys") val userIds: List<String>,
    val platform: String,
    val token: String,
)
