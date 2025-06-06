package net.primal.data.remote.api.users.model

import kotlinx.serialization.Serializable

@Serializable
data class UsersRequestBody(val pubkeys: List<String>)
