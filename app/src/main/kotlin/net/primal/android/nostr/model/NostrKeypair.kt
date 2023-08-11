package net.primal.android.nostr.model

import fr.acinq.secp256k1.Hex
import kotlinx.serialization.Serializable
import net.primal.android.crypto.Bech32
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.bech32ToHex
import net.primal.android.crypto.toHex
import net.primal.android.crypto.toNpub
import net.primal.android.crypto.toNsec

@Serializable
class HexVariant internal constructor(
    val pubkey: String,
    val privkey: String?
) {
    companion object {
        fun nostrKeypair(hexPubkey: String, hexPrivkey: String? = null): NostrKeypair {
            val npub = Hex.decode(hexPubkey).toNpub()

            var nsec: String? = null

            if (hexPrivkey != null) {
                nsec = Hex.decode(hexPrivkey).toNsec()
            }

            val hexVariant = HexVariant(hexPubkey, hexPrivkey)
            val nVariant = NVariant(npub, nsec)

            return NostrKeypair(hexVariant, nVariant)
        }

        fun hexPrivkeyToPubkey(hexPrivkey: String): String {
            val bytes = Hex.decode(hexPrivkey)

            val result = CryptoUtils.publicKeyCreate(bytes)

            return result.toHex()
        }
    }
}

@Serializable
class NVariant internal constructor(
    val npub: String,
    val nsec: String?
) {
    companion object {
        fun nostrKeypair(npub: String, nsec: String?): NostrKeypair {
            val pubkey = npub.bech32ToHex()

            var privkey: String? = null

            if(nsec != null) {
                privkey = nsec.bech32ToHex()
            }

            val hexVariant = HexVariant(pubkey, privkey)
            val nVariant = NVariant(npub, nsec)

            return NostrKeypair(hexVariant, nVariant)
        }
    }
}

@Serializable
class NostrKeypair internal constructor(
    val hexVariant: HexVariant,
    val nVariant: NVariant
)


