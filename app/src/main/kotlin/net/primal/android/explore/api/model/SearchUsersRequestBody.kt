package net.primal.android.explore.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchUsersRequestBody(
    @SerialName("query") val query: String,
    @SerialName("limit") val limit: Int,
)
