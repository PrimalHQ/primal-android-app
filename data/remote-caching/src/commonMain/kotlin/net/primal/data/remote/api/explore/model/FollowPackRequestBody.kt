package net.primal.data.remote.api.explore.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowPackRequestBody(
    @SerialName("pubkey") val authorId: String,
    val identifier: String,
)
