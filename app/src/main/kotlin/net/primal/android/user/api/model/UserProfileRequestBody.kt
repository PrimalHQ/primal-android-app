package net.primal.android.user.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileRequestBody(
    val pubkey: String,
)
