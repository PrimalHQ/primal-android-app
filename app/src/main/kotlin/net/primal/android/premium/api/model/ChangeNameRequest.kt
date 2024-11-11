package net.primal.android.premium.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangeNameRequest(
    val name: String,
)
