package net.primal.android.settings.wallet.domain

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class PrimalWalletNwc(
    val callback: String,
    val appName: String? = null,
    val appIcon: String? = null,
)

fun String.parseAsPrimalWalletNwc(): PrimalWalletNwc {
    val uri = Uri.parse(this)

    val callback = uri.getQueryParameterOrNull("callback")
    val appIcon = uri.getQueryParameterOrNull("appicon")
    val appName = uri.getQueryParameterOrNull("appname")

    if (uri.host != "connect" || callback == null) {
        throw PrimalWalletNwcParseException()
    }

    return PrimalWalletNwc(
        callback = callback,
        appName = appName,
        appIcon = appIcon,
    )
}

private fun Uri.getQueryParameterOrNull(key: String): String? {
    return runCatching { this.getQueryParameter(key) }.getOrNull()
}

fun String.isPrimalWalletNwcUrl(): Boolean {
    return try {
        this.parseAsPrimalWalletNwc()
        true
    } catch (error: PrimalWalletNwcParseException) {
        false
    }
}

class PrimalWalletNwcParseException : RuntimeException()
