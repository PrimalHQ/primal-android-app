package net.primal.android.user.domain

import android.net.Uri
import fr.acinq.secp256k1.Hex
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toHex

fun String.parseNWCUrl(): NostrWallet {
    val uri = Uri.parse(this)

    val host = uri.host
    val pubkey = when {
        host != null && host.toByteArray(Charsets.UTF_8).size == 64 -> host
        else -> throw NWCParseException()
    }

    val relay = uri.getQueryParameter("relay") ?: throw NWCParseException()
    val lud16 = uri.getQueryParameter("lud16")

    val secretParam = uri.getQueryParameter("secret")
    val keypairSecret: String = when {
        secretParam != null && secretParam.toByteArray().size == 64 -> secretParam
        else -> throw NWCParseException()
    }

    val keypairPubkey = CryptoUtils.publicKeyCreate(Hex.decode(keypairSecret)).toHex()

    return NostrWallet(
        pubkey = pubkey,
        lightningAddress = lud16,
        relays = listOf(relay),
        keypair = NostrWalletKeypair(privateKey = keypairSecret, pubkey = keypairPubkey),
    )
}

class NWCParseException : RuntimeException()
