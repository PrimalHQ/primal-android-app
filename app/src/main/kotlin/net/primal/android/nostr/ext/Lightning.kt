package net.primal.android.nostr.ext

import net.primal.android.crypto.Bech32

fun String.toLightningUrlOrNull(): String? {
    val parts = this.split("@")
    if (parts.count() != 2) return null

    val host = parts[1]
    val lnurlp = parts[0]

    val url = "https://$host/.well-known/lnurlp/$lnurlp)";

    return Bech32.encodeBytes("lnurl", this.toByteArray(), Bech32.Encoding.Bech32)
}
