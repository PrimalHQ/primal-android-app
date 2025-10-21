package net.primal.data.account.repository.repository

import io.ktor.http.Url
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.data.account.remote.command.model.NostrCommandResponse
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.repository.ConnectionRepository

@OptIn(ExperimentalUuidApi::class)
class SignerConnectionInitializer internal constructor(
    private val connectionRepository: ConnectionRepository,
    private val nostrRelayManager: NostrRelayManager,
) {
    suspend fun initialize(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
    ): Result<Unit> =
        runCatching {
            val (appConnection, secret) = parseConnectionUrlOrThrow(
                signerPubKey = signerPubKey,
                userPubKey = userPubKey,
                connectionUrl = connectionUrl,
            )

            nostrRelayManager.connectToRelays(relays = appConnection.relays.toSet())
            nostrRelayManager.sendResponse(
                relays = appConnection.relays,
                clientPubKey = appConnection.clientPubKey,
                response = NostrCommandResponse(
                    /*
                        TODO(marko): what should be the `id`? Nip doesn't define this.
                         Can it be some random value? I don't see why not.
                     */
                    id = Uuid.random().toString(),
                    result = secret,
                ),
            )
            /*
            TODO(marko): if sending the event fails, we shouldn't persist do db.
                right now we have no idea if it was successful so this should be addressed first.
             */

            connectionRepository.saveConnection(secret = secret, connection = appConnection)
        }

    private fun parseConnectionUrlOrThrow(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
    ): Pair<AppConnection, String> {
        if (!connectionUrl.startsWith(prefix = "nostrconnect://", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid `connectionUrl`. It should start with `nostrconnect://`.")
        }

        val parsedUrl = Url(urlString = connectionUrl)
        val clientPubKey = parsedUrl.host
        val relays = extractRelaysOrThrow(parsedUrl)
        val secret = extractSecretOrThrow(parsedUrl)
        val perms = extractPermsOrEmpty(parsedUrl)
        val name = parsedUrl.parameters["name"]
        val url = parsedUrl.parameters["url"]
        val image = parsedUrl.parameters["image"]

        return AppConnection(
            connectionId = Uuid.random().toString(),
            userPubKey = userPubKey,
            signerPubKey = signerPubKey,
            clientPubKey = clientPubKey,
            relays = relays,
            name = name,
            url = url,
            image = image,
            permissions = perms,
        ) to secret
    }

    private fun extractRelaysOrThrow(url: Url): List<String> =
        url.parameters.getAll("relay")
            ?: throw IllegalArgumentException(
                "No `relay` fields found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractSecretOrThrow(url: Url): String =
        url.parameters["secret"]
            ?: throw IllegalArgumentException(
                "No `secret` field found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractPermsOrEmpty(url: Url): List<AppPermission> {
        /* TODO(marko): actually parse permissions */
        return emptyList()
    }
}
