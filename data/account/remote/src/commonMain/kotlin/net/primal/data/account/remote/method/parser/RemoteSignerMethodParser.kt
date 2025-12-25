package net.primal.data.account.remote.method.parser

import io.github.aakira.napier.Napier
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.Result
import net.primal.core.utils.recover
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.method.model.NostrUnsignedEventNoPubkey
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodDecryptException
import net.primal.data.account.remote.method.model.RemoteSignerMethodParseException
import net.primal.data.account.remote.method.model.RemoteSignerMethodRequest
import net.primal.data.account.remote.method.model.RemoteSignerMethodType
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerMethodParser(
    private val nostrEncryptionService: NostrEncryptionService,
) {

    fun parseNostrEvent(event: NostrEvent, signerKeyPair: NostrKeyPair): Result<RemoteSignerMethod> {
        val decryptedContent = event.decryptContent(signerKeyPair = signerKeyPair).getOrNull()
            ?: return Result.failure(RemoteSignerMethodDecryptException(nostrEvent = event))

        return event.parseDecryptedContent(decryptedContent = decryptedContent).recover {
            Napier.w(throwable = it) { "Method content decryption failed." }
            val requestId = decryptedContent.decodeFromJsonStringOrNull<RemoteSignerMethodRequest>()?.id
            throw RemoteSignerMethodParseException(
                nostrEvent = event,
                requestId = requestId,
                cause = it,
            )
        }
    }

    private fun NostrEvent.decryptContent(signerKeyPair: NostrKeyPair): Result<String> =
        nostrEncryptionService.nip44Decrypt(
            ciphertext = this.content,
            privateKey = signerKeyPair.privateKey,
            pubKey = this.pubKey,
        )

    private fun NostrEvent.parseDecryptedContent(decryptedContent: String): Result<RemoteSignerMethod> {
        val request = decryptedContent.decodeFromJsonStringOrNull<RemoteSignerMethodRequest>()
            ?: return Result.failure(
                IllegalArgumentException(
                    "Failed to parse given content. Raw: $decryptedContent",
                ),
            )

        return runCatching {
            when (request.method) {
                RemoteSignerMethodType.Connect ->
                    RemoteSignerMethod.Connect(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                        remoteSignerPubkey = request.params[0],
                        secret = request.params.getOrNull(1),
                        requestedPermissions = request.params.getOrNull(2)?.split(",") ?: emptyList(),
                    )

                RemoteSignerMethodType.Ping ->
                    RemoteSignerMethod.Ping(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                    )

                RemoteSignerMethodType.SignEvent ->
                    RemoteSignerMethod.SignEvent(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                        unsignedEvent = request.params[0].decodeFromJsonStringOrNull<NostrUnsignedEventNoPubkey>()
                            ?: throw IllegalArgumentException(
                                "Couldn't parse `NostrUnsignedEvent` from received SignEvent request." +
                                    " Raw: ${request.params[0]}",
                            ),
                    )

                RemoteSignerMethodType.GetPublicKey ->
                    RemoteSignerMethod.GetPublicKey(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                    )

                RemoteSignerMethodType.Nip04Encrypt ->
                    RemoteSignerMethod.Nip04Encrypt(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                RemoteSignerMethodType.Nip04Decrypt ->
                    RemoteSignerMethod.Nip04Decrypt(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )

                RemoteSignerMethodType.Nip44Encrypt ->
                    RemoteSignerMethod.Nip44Encrypt(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                RemoteSignerMethodType.Nip44Decrypt ->
                    RemoteSignerMethod.Nip44Decrypt(
                        id = request.id,
                        clientPubKey = this.pubKey,
                        requestedAt = this.createdAt,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )
            }
        }
    }
}
