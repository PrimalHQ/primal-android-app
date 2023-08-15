package net.primal.android.user.domain

import android.net.Uri

fun String.parseNWCUrl(): NostrWallet {
    val uri = Uri.parse(this)

    val host =  uri.host
    val pubkey = when {
        host != null && host.toByteArray(Charsets.UTF_8).size == 64 -> host
        else -> throw NWCParseException()
    }

    val relay = uri.getQueryParameter("relay") ?: throw NWCParseException()
    val lud16 = uri.getQueryParameter("lud16") ?: throw NWCParseException()

    val secretParam = uri.getQueryParameter("secret")
    val secret: String = when {
        secretParam != null && secretParam.toByteArray().size == 64 -> secretParam
        else -> throw NWCParseException()
    }

    return NostrWallet(pubkey = pubkey, lud16 = lud16, relayUrl = relay, secret = secret)
}

class NWCParseException : RuntimeException()
