package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils

@OptIn(ExperimentalEncodingApi::class)
fun signNwcResponseNostrEvent(request: WalletNwcRequest, responseJson: String): SignResult {
    val connection = request.connection

    val tags = listOf(
        connection.secretPubKey.asPubkeyTag(),
        request.eventId.asEventIdTag(),
    )

    val encrypted = CryptoUtils.encrypt(
        msg = responseJson,
        privateKey = Hex.decode(connection.serviceKeyPair.privateKey),
        pubKey = Hex.decode(connection.secretPubKey),
    )

    val unsigned = NostrUnsignedEvent(
        pubKey = connection.serviceKeyPair.pubKey,
        kind = NostrEventKind.NwcResponse.value,
        content = encrypted,
        tags = tags,
    )

    return runCatching {
        SignResult.Signed(
            unsigned.signOrThrow(hexPrivateKey = Hex.decode(connection.serviceKeyPair.privateKey)),
        )
    }.getOrElse {
        SignResult.Rejected(SignatureException(cause = it))
    }
}
