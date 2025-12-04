package net.primal.data.account.remote.method.model

import kotlinx.serialization.Serializable
import net.primal.data.account.remote.method.model.serializer.RemoteSignerMethodResponseSerializer

@Serializable(with = RemoteSignerMethodResponseSerializer::class)
sealed class RemoteSignerMethodResponse {
    abstract val id: String
    abstract val clientPubKey: String

    @Serializable
    data class Success(
        override val id: String,
        override val clientPubKey: String,
        val result: String,
    ) : RemoteSignerMethodResponse()

    @Serializable
    data class Error(
        override val id: String,
        override val clientPubKey: String,
        val error: String,
    ) : RemoteSignerMethodResponse()

    fun assignClientPubKey(clientPubKey: String) =
        when (this) {
            is Error -> copy(clientPubKey = clientPubKey)
            is Success -> copy(clientPubKey = clientPubKey)
        }
}
