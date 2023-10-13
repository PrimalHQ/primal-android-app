package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class GetUserProfilesRequest(
    val pubkeys: List<JsonPrimitive> = emptyList()
)