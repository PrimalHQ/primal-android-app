package net.primal.core.networking.nwc

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils

@OptIn(ExperimentalEncodingApi::class)
internal inline fun <reified T> signNwcRequestNostrEvent(
    nwc: NostrWalletConnect,
    request: NwcWalletRequest<T>,
): SignResult {
    val tags = listOf(nwc.pubkey.asPubkeyTag())

    val plaintext = CommonJson.encodeToString(request)

    val encrypted = CryptoUtils.encrypt(
        msg = plaintext,
        privateKey = Hex.decode(nwc.keypair.privateKey),
        pubKey = Hex.decode(nwc.pubkey),
    )

    val unsigned = NostrUnsignedEvent(
        pubKey = nwc.keypair.pubkey,
        kind = NostrEventKind.NwcRequest.value,
        content = encrypted,
        tags = tags,
    )

    return runCatching {
        SignResult.Signed(
            unsigned.signOrThrow(hexPrivateKey = Hex.decode(nwc.keypair.privateKey)),
        )
    }.getOrElse {
        SignResult.Rejected(SignatureException(cause = it))
    }
}
