package net.primal.android.redeem.utils

import androidx.core.net.toUri

private const val NOSTR_CONNECT_SCHEME = "nostrconnect"

fun String.isNostrConnectUrl(): Boolean {
    return this.startsWith("$NOSTR_CONNECT_SCHEME://", ignoreCase = true)
}

private fun String.getNostrConnectQueryParameter(key: String): String? {
    if (!this.isNostrConnectUrl()) return null
    return try {
        this.toUri().getQueryParameter(key)
    } catch (_: Exception) {
        null
    }
}

fun String.getNostrConnectName(): String? = this.getNostrConnectQueryParameter("name")

fun String.getNostrConnectUrl(): String? = this.getNostrConnectQueryParameter("url")

fun String.getNostrConnectImage(): String? = this.getNostrConnectQueryParameter("image")
