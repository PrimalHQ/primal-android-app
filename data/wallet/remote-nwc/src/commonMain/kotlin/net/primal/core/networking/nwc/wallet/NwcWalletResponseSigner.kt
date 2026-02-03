package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.core.networking.nwc.nip47.NwcEncryptionScheme
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequestException
import net.primal.core.nips.encryption.service.NostrEncryptionService
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
import net.primal.domain.nostr.findFirstEncryptionTag

@OptIn(ExperimentalEncodingApi::class)
fun signNwcResponseNostrEvent(
    request: WalletNwcRequest,
    responseJson: String,
    encryptionService: NostrEncryptionService,
): SignResult {
    val connection = request.connection
    val encryptionScheme = request.encryptionScheme

    val tags = listOf(
        connection.secretPubKey.asPubkeyTag(),
        request.eventId.asEventIdTag(),
    )

    return runCatching {
        val encrypted = encryptContent(
            plaintext = responseJson,
            privateKey = connection.serviceKeyPair.privateKey,
            pubKey = connection.secretPubKey,
            encryptionScheme = encryptionScheme,
            encryptionService = encryptionService,
        )

        val unsigned = NostrUnsignedEvent(
            pubKey = connection.serviceKeyPair.pubKey,
            kind = NostrEventKind.NwcResponse.value,
            content = encrypted,
            tags = tags,
        )

        SignResult.Signed(
            unsigned.signOrThrow(hexPrivateKey = Hex.decode(connection.serviceKeyPair.privateKey)),
        )
    }.getOrElse {
        SignResult.Rejected(SignatureException(cause = it))
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun signNwcErrorResponseNostrEvent(
    error: WalletNwcRequestException,
    responseJson: String,
    encryptionService: NostrEncryptionService,
): SignResult {
    val connection = error.connection
    val eventId = error.nostrEvent.id

    val encryptionTag = error.nostrEvent.tags.findFirstEncryptionTag()
    val encryptionScheme = NwcEncryptionScheme.fromValueOrDefault(encryptionTag)

    val tags = listOf(
        connection.secretPubKey.asPubkeyTag(),
        eventId.asEventIdTag(),
    )

    return runCatching {
        val encrypted = encryptContent(
            plaintext = responseJson,
            privateKey = connection.serviceKeyPair.privateKey,
            pubKey = connection.secretPubKey,
            encryptionScheme = encryptionScheme,
            encryptionService = encryptionService,
        )

        val unsigned = NostrUnsignedEvent(
            pubKey = connection.serviceKeyPair.pubKey,
            kind = NostrEventKind.NwcResponse.value,
            content = encrypted,
            tags = tags,
        )

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
    supportedEncryption: List<NwcEncryptionScheme>,
    supportedNotifications: List<String>,
    supportedMethods: List<String>,
): SignResult {
    val content = supportedMethods.joinToString(" ")

    val encryptionTag = supportedEncryption.takeIf { it.isNotEmpty() }
        ?.joinToString(" ") { it.value }?.asEncryptionTag()

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

private fun encryptContent(
    plaintext: String,
    privateKey: String,
    pubKey: String,
    encryptionScheme: NwcEncryptionScheme,
    encryptionService: NostrEncryptionService,
): String {
    return when (encryptionScheme) {
        NwcEncryptionScheme.NIP44 -> encryptionService.nip44Encrypt(
            privateKey = privateKey,
            pubKey = pubKey,
            plaintext = plaintext,
        ).getOrThrow()

        NwcEncryptionScheme.NIP04 -> encryptionService.nip04Encrypt(
            privateKey = privateKey,
            pubKey = pubKey,
            plaintext = plaintext,
        ).getOrThrow()
    }
}
