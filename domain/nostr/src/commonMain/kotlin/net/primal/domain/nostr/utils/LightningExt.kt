package net.primal.domain.nostr.utils

import net.primal.domain.nostr.cryptography.utils.bechToBytesOrThrow
import net.primal.domain.nostr.cryptography.utils.urlToLnUrlHrp

fun String.parseAsLNUrlOrNull(): String? {
    val parts = this.split("@")
    if (parts.count() != 2) return null

    val host = parts[1]
    val lnurlp = parts[0]

    return "https://$host/.well-known/lnurlp/$lnurlp"
}

fun String.decodeLNUrlOrNull(): String? =
    runCatching {
        val byteArray = this.bechToBytesOrThrow(hrp = "lnurl")
        byteArray.decodeToString()
    }.getOrNull()

fun String.ensureEncodedLnUrl(): String =
    if (startsWith("http://") || startsWith("https://")) {
        this.urlToLnUrlHrp()
    } else {
        this
    }

fun String.stripLightningPrefix() = this.removePrefix("lightning:")
