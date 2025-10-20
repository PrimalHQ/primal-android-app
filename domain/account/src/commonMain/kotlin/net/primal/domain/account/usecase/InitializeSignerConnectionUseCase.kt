package net.primal.domain.account.usecase

import io.ktor.http.Url
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.repository.ConnectionRepository

@OptIn(ExperimentalUuidApi::class)
class InitializeSignerConnectionUseCase(
    private val connectionRepository: ConnectionRepository,
) {
    /**
     * Use case should perform following actions in order, failure of one fails the process:
     * - Parse `connectionUrl` and retrieve necessary data.
     * - Send `connect` response event to relays from the `connectionUrl`.
     * - Save the data to the db.
     */
    suspend fun invoke(userPubKey: String, connectionUrl: String): Result<Unit> =
        runCatching {
            val (appConnection, secret) = parseConnectionUrlOrThrow(
                userPubKey = userPubKey,
                connectionUrl = connectionUrl,
            )

            /* TODO(marko): send connect response with secret.
                We need to communicate with relays now. Does this conform us to `repository` module?
                    Or should we expose communication layer to the outside world? Perhaps not.
             */

            connectionRepository.saveConnection(secret = secret, connection = appConnection)
        }

    private fun parseConnectionUrlOrThrow(userPubKey: String, connectionUrl: String): Pair<AppConnection, String> {
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
                message = "No `relay` fields found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractSecretOrThrow(url: Url): String =
        url.parameters["secret"]
            ?: throw IllegalArgumentException(
                message = "No `secret` field found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractPermsOrEmpty(url: Url): List<AppPermission> {
        /* TODO(marko): actually parse permissions */
        return emptyList()
    }
}
