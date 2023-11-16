package net.primal.android.wallet.api

import net.primal.android.crypto.bechToBytes

fun String.parseAsLNUrlOrNull(): String? {
    val parts = this.split("@")
    if (parts.count() != 2) return null

    val host = parts[1]
    val lnurlp = parts[0]

    return "https://$host/.well-known/lnurlp/$lnurlp"
}

fun String.decodeLNUrlOrNull(): String? {
    return try {
        this.bechToBytes(hrp = "lnurl").let {
            String(it)
        }
    } catch (error: IllegalArgumentException) {
        null
    }
}
