package net.primal.android.config.domain

private const val CONFIG_CACHE_API = "wss://cache1.primal.net/v1"
private const val CONFIG_UPLOAD_API = "wss://uploads.primal.net/v1"
private const val CONFIG_WALLET_API = "wss://cache1.primal.net/v1"

val DEFAULT_APP_CONFIG = AppConfig(
    cacheUrl = CONFIG_CACHE_API,
    uploadUrl = CONFIG_UPLOAD_API,
    walletUrl = CONFIG_WALLET_API,
)
