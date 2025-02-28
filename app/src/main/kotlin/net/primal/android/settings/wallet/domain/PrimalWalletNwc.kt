package net.primal.android.settings.wallet.domain

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class PrimalWalletNwc(
    val callback: String? = null,
    val appName: String? = null,
    val appIcon: String? = null,
)

fun String.parseAsPrimalWalletNwc(): PrimalWalletNwc {
    val uri = Uri.parse(this)

    val callback = uri.getQueryParameterOrNull("callback")
    val appIcon = uri.getQueryParameterOrNull("appicon")
    val appName = uri.getQueryParameterOrNull("appname")

    if (uri.host != "connect") throw PrimalWalletNwcParseException()

    return PrimalWalletNwc(
        callback = callback,
        appName = appName,
        appIcon = appIcon,
    )
}

private fun Uri.getQueryParameterOrNull(key: String): String? {
    return runCatching { this.getQueryParameter(key) }.getOrNull()
}

class PrimalWalletNwcParseException : RuntimeException()
