package net.primal.android.premium.api.model

import kotlinx.serialization.Serializable

@Serializable
data class NameAvailableRequest(
    val name: String,
)
