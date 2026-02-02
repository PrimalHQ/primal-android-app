package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequestException
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asEncryptionTag
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asNotificationsTag
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

@OptIn(ExperimentalEncodingApi::class)
fun signNwcErrorResponseNostrEvent(error: WalletNwcRequestException, responseJson: String): SignResult {
    val connection = error.connection
    val eventId = error.nostrEvent.id

    val tags = listOf(
        connection.secretPubKey.asPubkeyTag(),
        eventId.asEventIdTag(),
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

@OptIn(ExperimentalEncodingApi::class)
fun signNwcInfoNostrEvent(
    connection: NwcConnection,
    supportedEncryption: List<String>,
    supportedNotifications: List<String>,
    supportedMethods: List<String>,
): SignResult {
    val content = supportedMethods.joinToString(" ")

    val encryptionTag = supportedEncryption.takeIf { it.isNotEmpty() }
        ?.joinToString(" ")?.asEncryptionTag()

    val notificationsTag = supportedNotifications.takeIf { it.isNotEmpty() }
        ?.joinToString(" ")?.asNotificationsTag()

    val unsigned = NostrUnsignedEvent(
        pubKey = connection.serviceKeyPair.pubKey,
        kind = NostrEventKind.WalletInfo.value,
        content = content,
        tags = listOfNotNull(encryptionTag, notificationsTag),
    )

    return runCatching {
        SignResult.Signed(
            unsigned.signOrThrow(hexPrivateKey = Hex.decode(connection.serviceKeyPair.privateKey)),
        )
    }.getOrElse {
        SignResult.Rejected(SignatureException(cause = it))
    }
}
