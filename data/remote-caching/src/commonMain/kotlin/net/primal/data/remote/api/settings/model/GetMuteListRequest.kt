package net.primal.data.remote.api.settings.model

import kotlinx.serialization.Serializable

@Serializable
data class GetMuteListRequest(
    val pubkey: String,
)
