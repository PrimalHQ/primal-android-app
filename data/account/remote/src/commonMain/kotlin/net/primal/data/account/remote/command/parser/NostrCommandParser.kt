package net.primal.data.account.remote.command.parser

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.data.account.remote.command.model.NostrCommandMethod
import net.primal.data.account.remote.command.model.NostrCommandRequest
import net.primal.domain.nostr.NostrUnsignedEvent

internal class NostrCommandParser {

    fun parse(clientPubkey: String, content: String): Result<NostrCommand> {
        val request = content.decodeFromJsonStringOrNull<NostrCommandRequest>()
            ?: return Result.failure(IllegalArgumentException("Failed to parse given content as `NostrCommandRequest`. Raw: $content"))

        return runCatching {
            when (request.method) {
                NostrCommandMethod.Connect ->
                    NostrCommand.Connect(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        remoteSignerPubkey = request.params[0],
                        secret = request.params[1],
                        requestedPermissions = request.params[2].split(","),
                    )

                NostrCommandMethod.Ping ->
                    NostrCommand.Ping(
                        id = request.id,
                        clientPubKey = clientPubkey,
                    )

                NostrCommandMethod.SignEvent ->
                    NostrCommand.SignEvent(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        unsignedEvent = request.params[0].decodeFromJsonStringOrNull<NostrUnsignedEvent>()
                            ?: throw IllegalArgumentException(
                                "Couldn't parse `NostrUnsignedEvent` from received SignEvent request.",
                            ),
                    )

                NostrCommandMethod.GetPublicKey ->
                    NostrCommand.GetPublicKey(
                        id = request.id,
                        clientPubKey = clientPubkey,
                    )

                NostrCommandMethod.Nip04Encrypt ->
                    NostrCommand.Nip04Encrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                NostrCommandMethod.Nip04Decrypt ->
                    NostrCommand.Nip04Decrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )

                NostrCommandMethod.Nip44Encrypt ->
                    NostrCommand.Nip44Encrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                NostrCommandMethod.Nip44Decrypt ->
                    NostrCommand.Nip44Decrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )
            }
        }
    }
}
