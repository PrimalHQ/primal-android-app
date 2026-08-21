package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import io.github.aakira.napier.Napier
import net.primal.core.utils.getOrDefault
import net.primal.core.utils.runCatching
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.utils.CryptoUtils

/**
 * Authenticates incoming NIP-47 requests before they are decrypted or acted upon.
 *
 * Per NIP-47 the client secret in a `nostr+walletconnect://` URI is the client's signing key,
 * so a wallet service must only honour requests actually signed by that key. Resolving a
 * connection by the `p` tag proves nothing: the service pubkey is public, and anyone can
 * encrypt to it.
 */
class NwcRequestAuthenticator {

    fun authenticate(event: NostrEvent, connection: NwcConnection): Boolean {
        if (event.pubKey != connection.secretPubKey) {
            Napier.w(tag = TAG) { "Rejected NWC request ${event.id}: author is not the authorized client." }
            return false
        }

        val validSignature = runCatching {
            CryptoUtils.verify(
                signature = Hex.decode(event.sig),
                hash = Hex.decode(event.id),
                pubKey = Hex.decode(event.pubKey),
            )
        }.getOrDefault(false)

        if (!validSignature) {
            Napier.w(tag = TAG) { "Rejected NWC request ${event.id}: invalid signature." }
        }

        return validSignature
    }

    companion object {
        private const val TAG = "NwcRequestAuthenticator"
    }
}
