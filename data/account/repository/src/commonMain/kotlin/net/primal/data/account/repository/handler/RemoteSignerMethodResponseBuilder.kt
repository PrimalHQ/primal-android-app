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
    suspend fun build(command: RemoteSignerMethod): RemoteSignerMethodResponse {
        return when (command) {
            is RemoteSignerMethod.Connect -> connect(command)
            is RemoteSignerMethod.GetPublicKey -> getPublicKey(command)
            is RemoteSignerMethod.Nip04Decrypt -> nip04Decrypt(command)
            is RemoteSignerMethod.Nip04Encrypt -> nip04Encrypt(command)
            is RemoteSignerMethod.Nip44Decrypt -> nip44Decrypt(command)
            is RemoteSignerMethod.Nip44Encrypt -> nip44Encrypt(command)
            is RemoteSignerMethod.Ping -> ping(command)
            is RemoteSignerMethod.SignEvent -> signEvent(command)
        }
    }

    private fun ping(command: RemoteSignerMethod.Ping): RemoteSignerMethodResponse {
        return RemoteSignerMethodResponse(
            id = command.id,
            result = "pong",
        )
    }

    private fun connect(command: RemoteSignerMethod.Connect): RemoteSignerMethodResponse =
        RemoteSignerMethodResponse(
            id = command.id,
            result = "",
            error = "We don't accept incoming connection requests. Please scan or enter `nostrconnect://` url to initiate a connection.",
        )

    private suspend fun signEvent(command: RemoteSignerMethod.SignEvent): RemoteSignerMethodResponse {
        return nostrEventSignatureHandler.signNostrEvent(
            unsignedNostrEvent = command.unsignedEvent,
        ).getOrNull()?.let {
            RemoteSignerMethodResponse(
                id = command.id,
                result = it.encodeToJsonString(),
            )
        } ?: RemoteSignerMethodResponse(
            id = command.id,
            result = "",
            error = "Couldn't sign event.",
        )
    }

    private suspend fun nip44Encrypt(command: RemoteSignerMethod.Nip44Encrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip44Encrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    plaintext = command.plaintext,
                )
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip44 encryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun nip44Decrypt(command: RemoteSignerMethod.Nip44Decrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip44Decrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    ciphertext = command.ciphertext,
                )
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip44 decryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun nip04Encrypt(command: RemoteSignerMethod.Nip04Encrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip04Encrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    plaintext = command.plaintext,
                )
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip04 encryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun nip04Decrypt(command: RemoteSignerMethod.Nip04Decrypt): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip04Decrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    ciphertext = command.ciphertext,
                )
            }.fold(
                onSuccess = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip04 decryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun getPublicKey(command: RemoteSignerMethod.GetPublicKey): RemoteSignerMethodResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .fold(
                onSuccess = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    RemoteSignerMethodResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to process command: ${it.message}",
                    )
                },
            )
    }
}
