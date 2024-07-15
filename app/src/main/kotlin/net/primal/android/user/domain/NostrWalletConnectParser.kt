package net.primal.android.user.domain

import android.net.Uri
import fr.acinq.secp256k1.Hex
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toHex

fun String.parseNWCUrl(): NostrWalletConnect {
    val uri = Uri.parse(this)

    val host = uri.host
    val pubkey = when {
        host != null && host.toByteArray(Charsets.UTF_8).size == 64 -> host
        else -> null
    }

    val relay = uri.getQueryParameterOrNull("relay")
    val lud16 = uri.getQueryParameterOrNull("lud16")

    val secretParam = uri.getQueryParameterOrNull("secret")
    val keypairSecret = when {
        secretParam != null && secretParam.toByteArray().size == 64 -> secretParam
        else -> null
    }

    if (pubkey == null || relay == null || keypairSecret == null) {
        throw NWCParseException()
    }

    return NostrWalletConnect(
        pubkey = pubkey,
        lightningAddress = lud16,
        relays = listOf(relay),
        keypair = NostrWalletKeypair(
            privateKey = keypairSecret,
            pubkey = CryptoUtils.publicKeyCreate(Hex.decode(keypairSecret)).toHex(),
        ),
    )
}

private fun Uri.getQueryParameterOrNull(key: String): String? {
    return runCatching { this.getQueryParameter(key) }.getOrNull()
}

fun String.isNwcUrl(): Boolean {
    return try {
        this.parseNWCUrl()
        true
    } catch (error: NWCParseException) {
        false
    }
}

class NWCParseException : RuntimeException()
