package net.primal.android.user.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRequestBody(val pubkey: String)
