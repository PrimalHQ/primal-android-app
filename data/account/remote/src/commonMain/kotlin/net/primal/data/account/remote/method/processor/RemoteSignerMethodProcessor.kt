package net.primal.data.account.remote.method.processor

import com.vitorpamplona.quartz.nip44Encryption.Nip44v2
import io.github.aakira.napier.Napier
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodRequest
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.parser.RemoteSignerMethodParser
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.utils.assureValidNpub
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.bechToBytesOrThrow

class RemoteSignerMethodProcessor {
    private val remoteSignerMethodParser = RemoteSignerMethodParser()
    private val nip44 = Nip44v2()

    fun processNostrEvent(
        event: NostrEvent,
        signerKeyPair: NostrKeyPair,
        onFailure: (RemoteSignerMethodResponse.Error) -> Unit,
        onSuccess: (RemoteSignerMethod) -> Unit,
    ) {
        val decryptedContent = runCatching { event.decryptContent(signerKeyPair = signerKeyPair) }
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

    private fun NostrEvent.decryptContent(signerKeyPair: NostrKeyPair): String =
        nip44.decrypt(
            payload = this.content,
            privateKey = signerKeyPair.privateKey.assureValidNsec().bechToBytesOrThrow(),
            pubKey = this.pubKey.assureValidNpub().bechToBytesOrThrow(),
        )
}
