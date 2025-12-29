package net.primal.data.account.repository.builder

import net.primal.core.utils.fold
import net.primal.core.utils.mapCatching
import net.primal.core.utils.serialization.CommonJsonEncodeDefaults
import net.primal.data.account.remote.signer.model.RemoteSignerMethod
import net.primal.data.account.remote.signer.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.signer.model.withPubKey
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow

internal class RemoteSignerMethodResponseBuilder(
    private val connectionRepository: ConnectionRepository,
    private val nostrEventSignatureHandler: NostrEventSignatureHandler,
    private val nostrEncryptionHandler: NostrEncryptionHandler,
) {
    suspend fun build(method: RemoteSignerMethod): RemoteSignerMethodResponse {
        return when (method) {
            is RemoteSignerMethod.Connect -> connect(method)
            is RemoteSignerMethod.GetPublicKey -> getPublicKey(method)
            is RemoteSignerMethod.Nip04Decrypt -> nip04Decrypt(method)
            is RemoteSignerMethod.Nip04Encrypt -> nip04Encrypt(method)
            is RemoteSignerMethod.Nip44Decrypt -> nip44Decrypt(method)
            is RemoteSignerMethod.Nip44Encrypt -> nip44Encrypt(method)
            is RemoteSignerMethod.Ping -> ping(method)
            is RemoteSignerMethod.SignEvent -> signEvent(method)
        }
    }

    private fun ping(method: RemoteSignerMethod.Ping): RemoteSignerMethodResponse {
        return RemoteSignerMethodResponse.Success(
            id = method.id,
            result = "pong",
            clientPubKey = method.clientPubKey,
        )
    }

    private suspend fun connect(method: RemoteSignerMethod.Connect): RemoteSignerMethodResponse =
        connectionRepository.getConnectionByClientPubKey(clientPubKey = method.clientPubKey)
            .fold(
                onSuccess = { connection ->
                    if (method.secret.isNullOrBlank()) {
                        RemoteSignerMethodResponse.Success(
                            id = method.id,
                            result = "ack",
                            clientPubKey = method.clientPubKey,
                        )
                    } else {
                        RemoteSignerMethodResponse.Error(
                            id = method.id,
                            error = "We don't accept connect requests with new secret.",
                            clientPubKey = method.clientPubKey,
                        )
                    }
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "We don't accept incoming connection requests. " +
                            "Please scan or enter `nostrconnect://` url to initiate a connection.",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )

    private suspend fun signEvent(method: RemoteSignerMethod.SignEvent): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .mapCatching { userPubKey ->
                nostrEventSignatureHandler.signNostrEvent(
                    unsignedNostrEvent = method.unsignedEvent.withPubKey(pubkey = userPubKey),
                ).unwrapOrThrow()
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse.Success(
                        id = method.id,
                        result = CommonJsonEncodeDefaults.encodeToString(it),
                        clientPubKey = method.clientPubKey,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "Couldn't sign event.",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )
    }

    private suspend fun nip44Encrypt(method: RemoteSignerMethod.Nip44Encrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .mapCatching { userPubKey ->
                nostrEncryptionHandler.nip44Encrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    plaintext = method.plaintext,
                ).getOrThrow()
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse.Success(
                        id = method.id,
                        result = it,
                        clientPubKey = method.clientPubKey,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "Failed to run nip44 encryption. Reason: ${it.message}",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )
    }

    private suspend fun nip44Decrypt(method: RemoteSignerMethod.Nip44Decrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .mapCatching { userPubKey ->
                nostrEncryptionHandler.nip44Decrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    ciphertext = method.ciphertext,
                ).getOrThrow()
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse.Success(
                        id = method.id,
                        result = it,
                        clientPubKey = method.clientPubKey,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "Failed to run nip44 decryption. Reason: ${it.message}",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )
    }

    private suspend fun nip04Encrypt(method: RemoteSignerMethod.Nip04Encrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .mapCatching { userPubKey ->
                nostrEncryptionHandler.nip04Encrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    plaintext = method.plaintext,
                ).getOrThrow()
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse.Success(
                        id = method.id,
                        result = it,
                        clientPubKey = method.clientPubKey,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "Failed to run nip04 encryption. Reason: ${it.message}",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )
    }

    private suspend fun nip04Decrypt(method: RemoteSignerMethod.Nip04Decrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .mapCatching { userPubKey ->
                nostrEncryptionHandler.nip04Decrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    ciphertext = method.ciphertext,
                ).getOrThrow()
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse.Success(
                        id = method.id,
                        result = it,
                        clientPubKey = method.clientPubKey,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "Failed to run nip04 decryption. Reason: ${it.message}",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )
    }

    private suspend fun getPublicKey(method: RemoteSignerMethod.GetPublicKey): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .fold(
                onSuccess = {
                    RemoteSignerMethodResponse.Success(
                        id = method.id,
                        result = it,
                        clientPubKey = method.clientPubKey,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse.Error(
                        id = method.id,
                        error = "Failed to process command: ${it.message}",
                        clientPubKey = method.clientPubKey,
                    )
                },
            )
    }
}
