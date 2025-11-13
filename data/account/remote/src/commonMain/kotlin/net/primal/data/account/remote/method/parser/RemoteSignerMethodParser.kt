package net.primal.data.account.remote.method.parser

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.method.model.NostrUnsignedEventNoPubkey
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodRequest
import net.primal.data.account.remote.method.model.RemoteSignerMethodType

internal class RemoteSignerMethodParser {

    fun parse(
        clientPubkey: String,
        content: String,
        requestedAt: Long,
    ): Result<RemoteSignerMethod> {
        val request = content.decodeFromJsonStringOrNull<RemoteSignerMethodRequest>()
            ?: return Result.failure(
                IllegalArgumentException(
                    "Failed to parse given content. Raw: $content",
                ),
            )

        return runCatching {
            when (request.method) {
                RemoteSignerMethodType.Connect ->
                    RemoteSignerMethod.Connect(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                        remoteSignerPubkey = request.params[0],
                        secret = request.params.getOrNull(1),
                        requestedPermissions = request.params.getOrNull(2)?.split(",") ?: emptyList(),
                    )

                RemoteSignerMethodType.Ping ->
                    RemoteSignerMethod.Ping(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                    )

                RemoteSignerMethodType.SignEvent ->
                    RemoteSignerMethod.SignEvent(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                        unsignedEvent = request.params[0].decodeFromJsonStringOrNull<NostrUnsignedEventNoPubkey>()
                            ?: throw IllegalArgumentException(
                                "Couldn't parse `NostrUnsignedEvent` from received SignEvent request." +
                                    " Raw: ${request.params[0]}",
                            ),
                    )

                RemoteSignerMethodType.GetPublicKey ->
                    RemoteSignerMethod.GetPublicKey(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                    )

                RemoteSignerMethodType.Nip04Encrypt ->
                    RemoteSignerMethod.Nip04Encrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                RemoteSignerMethodType.Nip04Decrypt ->
                    RemoteSignerMethod.Nip04Decrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )

                RemoteSignerMethodType.Nip44Encrypt ->
                    RemoteSignerMethod.Nip44Encrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                        thirdPartyPubKey = request.params[0],
                        plaintext = request.params[1],
                    )

                RemoteSignerMethodType.Nip44Decrypt ->
                    RemoteSignerMethod.Nip44Decrypt(
                        id = request.id,
                        clientPubKey = clientPubkey,
                        requestedAt = requestedAt,
                        thirdPartyPubKey = request.params[0],
                        ciphertext = request.params[1],
                    )
            }
        }
    }
}
