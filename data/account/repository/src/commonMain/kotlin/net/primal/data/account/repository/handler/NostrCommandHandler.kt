package net.primal.data.account.repository.handler

import net.primal.core.utils.alsoCatching
import net.primal.core.utils.fold
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.data.account.remote.command.model.NostrCommandResponse
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.getOrNull

internal class NostrCommandHandler(
    private val connectionRepository: ConnectionRepository,
    private val nostrEventSignatureHandler: NostrEventSignatureHandler,
    private val nostrEncryptionHandler: NostrEncryptionHandler,
) {
    suspend fun handle(command: NostrCommand): NostrCommandResponse {
        return when (command) {
            is NostrCommand.Connect -> connect(command)
            is NostrCommand.GetPublicKey -> getPublicKey(command)
            is NostrCommand.Nip04Decrypt -> nip04Decrypt(command)
            is NostrCommand.Nip04Encrypt -> nip04Encrypt(command)
            is NostrCommand.Nip44Decrypt -> nip44Decrypt(command)
            is NostrCommand.Nip44Encrypt -> nip44Encrypt(command)
            is NostrCommand.Ping -> ping(command)
            is NostrCommand.SignEvent -> signEvent(command)
        }
    }

    private fun ping(command: NostrCommand.Ping): NostrCommandResponse {
        return NostrCommandResponse(
            id = command.id,
            result = "pong",
        )
    }

    private fun connect(command: NostrCommand.Connect): NostrCommandResponse {
        return NostrCommandResponse(
            id = command.id,
            result = command.secret,
        )
    }

    private suspend fun signEvent(command: NostrCommand.SignEvent): NostrCommandResponse {
        return nostrEventSignatureHandler.signNostrEvent(
            unsignedNostrEvent = command.unsignedEvent,
        ).getOrNull()?.let {
            NostrCommandResponse(
                id = command.id,
                result = it.encodeToJsonString(),
            )
        } ?: NostrCommandResponse(
            id = command.id,
            result = "",
            error = "Couldn't sign event.",
        )
    }

    private suspend fun nip44Encrypt(command: NostrCommand.Nip44Encrypt): NostrCommandResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip44Encrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    plaintext = command.plaintext,
                )
            }.fold(
                onSuccess = {
                    NostrCommandResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    NostrCommandResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip44 encryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun nip44Decrypt(command: NostrCommand.Nip44Decrypt): NostrCommandResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip44Decrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    ciphertext = command.ciphertext,
                )
            }.fold(
                onSuccess = {
                    NostrCommandResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    NostrCommandResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip44 decryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun nip04Encrypt(command: NostrCommand.Nip04Encrypt): NostrCommandResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip04Encrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    plaintext = command.plaintext,
                )
            }.fold(
                onSuccess = {
                    NostrCommandResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    NostrCommandResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip04 encryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun nip04Decrypt(command: NostrCommand.Nip04Decrypt): NostrCommandResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .alsoCatching { userPubKey ->
                nostrEncryptionHandler.nip04Decrypt(
                    userId = userPubKey,
                    participantId = command.thirdPartyPubKey,
                    ciphertext = command.ciphertext,
                )
            }.fold(
                onSuccess = {
                    NostrCommandResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    NostrCommandResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to run nip04 decryption. Reason: ${it.message}",
                    )
                },
            )
    }

    private suspend fun getPublicKey(command: NostrCommand.GetPublicKey): NostrCommandResponse {
        return connectionRepository.getUserPubKey(clientPubKey = command.clientPubKey)
            .fold(
                onSuccess = {
                    NostrCommandResponse(
                        id = command.id,
                        result = it,
                    )
                },
                onFailure = {
                    NostrCommandResponse(
                        id = command.id,
                        result = "",
                        error = "Failed to process command: ${it.message}",
                    )
                },
            )
    }
}
