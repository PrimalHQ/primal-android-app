package net.primal.domain.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsDescription(
    val description: String,
)
