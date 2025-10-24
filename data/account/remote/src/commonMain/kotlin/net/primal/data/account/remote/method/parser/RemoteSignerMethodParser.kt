package net.primal.data.account.remote.method.parser

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodRequest
import net.primal.data.account.remote.method.model.RemoteSignerMethodType
import net.primal.domain.nostr.NostrUnsignedEvent

internal class RemoteSignerMethodParser {

    fun parse(clientPubkey: String, content: String): Result<RemoteSignerMethod> {
        val request = content.decodeFromJsonStringOrNull<RemoteSignerMethodRequest>()
            ?: return Result.failure(
                IllegalArgumentException(
                    "Failed to parse given content as `NostrCommandRequest`. Raw: $content",
                ),
            )

        return runCatching {
            when (request.method) {
                RemoteSignerMethodType.Connect ->
                    RemoteSignerMethod.Connect(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        remoteSignerPubkey = request.params[0],
                        secret = request.params[1],
                        requestedPermissions = request.params[2].split(","),
                    )

                RemoteSignerMethodType.Ping ->
                    RemoteSignerMethod.Ping(
                        id = request.id,
                        clientPubKey = clientPubkey,
                    )

                RemoteSignerMethodType.SignEvent ->
                    RemoteSignerMethod.SignEvent(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        unsignedEvent = request.params[0].decodeFromJsonStringOrNull<NostrUnsignedEvent>()
                            ?: throw IllegalArgumentException(
                                "Couldn't parse `NostrUnsignedEvent` from received SignEvent request.",
                            ),
                    )

                RemoteSignerMethodType.GetPublicKey ->
                    RemoteSignerMethod.GetPublicKey(
                        id = request.id,
                        clientPubKey = clientPubkey,
                    )

                RemoteSignerMethodType.Nip04Encrypt ->
                    RemoteSignerMethod.Nip04Encrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                RemoteSignerMethodType.Nip04Decrypt ->
                    RemoteSignerMethod.Nip04Decrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )

                RemoteSignerMethodType.Nip44Encrypt ->
                    RemoteSignerMethod.Nip44Encrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                RemoteSignerMethodType.Nip44Decrypt ->
                    RemoteSignerMethod.Nip44Decrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )
            }
        }
    }
}
