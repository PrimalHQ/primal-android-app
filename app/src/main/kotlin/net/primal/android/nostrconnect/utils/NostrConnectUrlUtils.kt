package net.primal.android.nostrconnect.utils

import androidx.core.net.toUri

const val NOSTR_CONNECT_SCHEME = "nostrconnect"

fun String.isNostrConnectUrl(): Boolean {
    return this.startsWith("$NOSTR_CONNECT_SCHEME://", ignoreCase = true)
}

private fun String.getNostrConnectQueryParameter(key: String): String? {
    if (!this.isNostrConnectUrl()) return null
    return runCatching { this.toUri().getQueryParameter(key) }.getOrNull()
}

fun String.getNostrConnectName(): String? = this.getNostrConnectQueryParameter("name")

fun String.getNostrConnectUrl(): String? = this.getNostrConnectQueryParameter("url")

fun String.getNostrConnectImage(): String? = this.getNostrConnectQueryParameter("image")

fun String.getNostrConnectCallback(): String? = this.getNostrConnectQueryParameter("callback")

fun String.hasNwcOption(): Boolean {
    return this.getNostrConnectQueryParameter("nwc") == "1"
}
