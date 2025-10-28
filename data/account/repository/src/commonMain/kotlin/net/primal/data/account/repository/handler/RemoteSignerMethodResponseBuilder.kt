package net.primal.data.account.repository.handler

import net.primal.core.utils.alsoCatching
import net.primal.core.utils.fold
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.getOrNull

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

    private fun connect(method: RemoteSignerMethod.Connect): RemoteSignerMethodResponse =
        RemoteSignerMethodResponse.Error(
            id = method.id,
            error = "We don't accept incoming connection requests. " +
                "Please scan or enter `nostrconnect://` url to initiate a connection.",
            clientPubKey = method.clientPubKey,
        )

    private suspend fun signEvent(method: RemoteSignerMethod.SignEvent): RemoteSignerMethodResponse {
        return nostrEventSignatureHandler.signNostrEvent(
            unsignedNostrEvent = method.unsignedEvent,
        ).getOrNull()?.let {
            RemoteSignerMethodResponse.Success(
                id = method.id,
                result = it.encodeToJsonString(),
                clientPubKey = method.clientPubKey,
            )
        } ?: RemoteSignerMethodResponse.Error(
            id = method.id,
            error = "Couldn't sign event.",
            clientPubKey = method.clientPubKey,
        )
    }

    private suspend fun nip44Encrypt(method: RemoteSignerMethod.Nip44Encrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = method.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip44Encrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    plaintext = method.plaintext,
                )
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
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip44Decrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    ciphertext = method.ciphertext,
                )
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
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip04Encrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    plaintext = method.plaintext,
                )
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
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip04Decrypt(
                    userId = userPubKey,
                    participantId = method.thirdPartyPubKey,
                    ciphertext = method.ciphertext,
                )
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
