package net.primal.domain

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val cacheUrl: String,
    val cacheUrlOverride: Boolean = false,
    val uploadUrl: String,
    val walletUrl: String,
)
