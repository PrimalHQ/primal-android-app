package net.primal.android.wallet.api

fun String.toLightningUrlOrNull(): String? {
    val parts = this.split("@")
    if (parts.count() != 2) return null

    val host = parts[1]
    val lnurlp = parts[0]

    return  "https://$host/.well-known/lnurlp/$lnurlp"
}
