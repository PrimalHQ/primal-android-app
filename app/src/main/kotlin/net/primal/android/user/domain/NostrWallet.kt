package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class NostrWallet(
    val lud16: String,
    val relayUrl: String,
    val pubkey: String,
    val keypair: NostrWalletKeypair,
) {
    fun toStringUrl(): String {
        return "nostr+walletconnect://${pubkey}?relay=${relayUrl}&secret=${keypair.privkey}&lud16=${lud16}"
    }
}

@Serializable
data class NostrWalletKeypair(val privkey: String, val pubkey: String)
