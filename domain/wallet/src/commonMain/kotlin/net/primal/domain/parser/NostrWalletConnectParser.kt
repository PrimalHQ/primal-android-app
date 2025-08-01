package net.primal.domain.parser

import fr.acinq.secp256k1.Hex
import io.ktor.http.Url
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.toHex
import net.primal.domain.wallet.NostrWalletConnect
import net.primal.domain.wallet.NostrWalletKeypair

fun String.parseNWCUrl(): NostrWalletConnect {
    val uri = Url(this)

    val host = uri.host
    val pubkey = when {
        host.toByteArray(Charsets.UTF_8).size == 64 -> host
        else -> null
    }

    val relay = uri.parameters["relay"]
    val lud16 = uri.parameters["lud16"]

    val secretParam = uri.parameters["secret"]
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

fun String.isNwcUrl(): Boolean = runCatching { parseNWCUrl() }.isSuccess

class NWCParseException : RuntimeException()
