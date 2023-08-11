package net.primal.android.user.domain

import android.net.Uri
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.HexVariant
import net.primal.android.nostr.model.NostrKeypair

@Serializable
data class NostrWalletConnect(
    val pubkey: String,
    val lud16: String,
    val relayUrl: String,
    val keypair: NostrKeypair
)

fun String.toNostrWalletConnect(): NostrWalletConnect {
    val uri = Uri.parse(this)

    val pubkey = when {
        uri.host != null && uri.host?.toByteArray(Charsets.UTF_8)?.size == 64 -> uri.host!!
        else -> throw NostrWalletConnectParseException()
    }

    val relay = uri.getQueryParameter("relay") ?: throw NostrWalletConnectParseException()
    val lud16 = uri.getQueryParameter("lud16") ?: throw NostrWalletConnectParseException()

    val secretQueryParameter = uri.getQueryParameter("secret")

    val keypair: NostrKeypair = when {
        secretQueryParameter != null && secretQueryParameter.toByteArray().size == 64 -> {
            val pk = HexVariant.hexPrivkeyToPubkey(secretQueryParameter)
            HexVariant.nostrKeypair(pk, secretQueryParameter)
        }
        else -> throw NostrWalletConnectParseException()
    }

    return NostrWalletConnect(pubkey = pubkey, lud16 = lud16, relayUrl = relay, keypair = keypair)
}

fun NostrWalletConnect.toStringUrl(): String {
    return "nostr+walletconnect://${this.pubkey}?relay=${this.relayUrl}&secret=${this.keypair.hexVariant.privkey}&lud16=${this.lud16}"
}

class NostrWalletConnectParseException : RuntimeException()