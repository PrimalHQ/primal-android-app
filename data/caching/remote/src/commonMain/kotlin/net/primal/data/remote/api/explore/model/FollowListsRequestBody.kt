package net.primal.data.remote.api.explore.model

import kotlinx.serialization.Serializable

@Serializable
data class FollowListsRequestBody(
    val until: Long?,
    val limit: Int,
    val since: Long? = null,
    val offset: Int? = null,
)
