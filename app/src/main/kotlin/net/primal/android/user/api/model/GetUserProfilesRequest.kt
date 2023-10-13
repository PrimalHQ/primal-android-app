package net.primal.android.user.api.model

import kotlinx.serialization.Serializable

@Serializable
data class GetUserProfilesRequest(
    val pubkeys: Set<String> = emptySet()
)