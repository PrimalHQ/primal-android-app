package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable

@Serializable
data class GetMutelistRequest(
    val pubkey: String
)