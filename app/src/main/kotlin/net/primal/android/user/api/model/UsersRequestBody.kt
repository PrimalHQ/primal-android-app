package net.primal.android.user.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UsersRequestBody(val pubkeys: List<String>)
