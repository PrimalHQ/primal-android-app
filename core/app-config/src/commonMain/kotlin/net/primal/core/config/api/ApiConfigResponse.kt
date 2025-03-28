package net.primal.core.config.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiConfigResponse(
    @SerialName("mobile_cache_server_v1") val cacheServers: List<String>,
    @SerialName("upload_server_v1") val uploadServers: List<String>,
    @SerialName("wallet_server_v1") val walletServers: List<String>,
)
