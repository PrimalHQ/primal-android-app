package net.primal.data.account.remote.method.processor

import io.github.aakira.napier.Napier
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.Result
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodRequest
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.parser.RemoteSignerMethodParser
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerMethodProcessor(
    private val nostrEncryptionService: NostrEncryptionService,
) {
    private val remoteSignerMethodParser = RemoteSignerMethodParser()

    fun processNostrEvent(
        event: NostrEvent,
        signerKeyPair: NostrKeyPair,
        onFailure: (RemoteSignerMethodResponse.Error) -> Unit,
        onSuccess: (RemoteSignerMethod) -> Unit,
    ) {
        val decryptedContent = event.decryptContent(signerKeyPair = signerKeyPair)
            .onFailure {
                val message = "Failed to decrypt content. Raw: $event"
                Napier.w(throwable = it) { message }
                onFailure(
                    RemoteSignerMethodResponse.Error(
                        id = event.id,
                        error = message,
                        clientPubKey = event.pubKey,
                    ),
                )
            }.getOrNull() ?: return

        remoteSignerMethodParser.parse(
            clientPubkey = event.pubKey,
            content = decryptedContent,
            requestedAt = event.createdAt,
        ).onSuccess { command ->
            Napier.d(tag = "Signer") { "Received command: $command" }
            onSuccess(command)
        }.onFailure {
            val message = it.message ?: "There was an error while parsing method."
            Napier.w(throwable = it) { message }
            val id = decryptedContent.decodeFromJsonStringOrNull<RemoteSignerMethodRequest>()?.id
            onFailure(
                RemoteSignerMethodResponse.Error(
                    id = id ?: event.id,
                    error = message,
                    clientPubKey = event.pubKey,
                ),
            )
        }
    }

    private fun NostrEvent.decryptContent(signerKeyPair: NostrKeyPair): Result<String> =
        nostrEncryptionService.nip44Decrypt(
            ciphertext = this.content,
            privateKey = signerKeyPair.privateKey,
            pubKey = this.pubKey,
        )
}
