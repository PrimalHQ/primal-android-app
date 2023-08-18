package net.primal.android.nostr.ext

fun String.toLightningUrlOrNull(): String? {
    val parts = this.split("@")
    if (parts.count() != 2) return null

    val host = parts[1]
    val lnurlp = parts[0]

    val url = "https://$host/.well-known/lnurlp/$lnurlp";

    return url
}
