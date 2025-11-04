package net.primal.data.account.repository.repository

import io.ktor.http.Url
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.repository.ConnectionRepository

private const val NOSTR_CONNECT_PREFIX = "nostrconnect://"
private const val NAME_PARAM = "name"
private const val IMAGE_PARAM = "image"
private const val URL_PARAM = "url"
private const val RELAY_PARAM = "relay"
private const val SECRET_PARAM = "secret"

@OptIn(ExperimentalUuidApi::class)
class SignerConnectionInitializer internal constructor(
    private val connectionRepository: ConnectionRepository,
    private val nostrRelayManager: NostrRelayManager,
) {
    suspend fun initialize(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
    ): Result<AppConnection> =
        runCatching {
            val (appConnection, secret) = parseConnectionUrlOrThrow(
                signerPubKey = signerPubKey,
                userPubKey = userPubKey,
                connectionUrl = connectionUrl,
            )

            nostrRelayManager.connectToRelays(relays = appConnection.relays.toSet())
            nostrRelayManager.sendResponse(
                relays = appConnection.relays,
                response = RemoteSignerMethodResponse.Success(
                    id = Uuid.random().toString(),
                    result = secret,
                    clientPubKey = appConnection.clientPubKey,
                ),
            )

            connectionRepository.saveConnection(secret = secret, connection = appConnection)

            appConnection
        }

    private fun parseConnectionUrlOrThrow(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
    ): Pair<AppConnection, String> {
        if (!connectionUrl.startsWith(prefix = NOSTR_CONNECT_PREFIX, ignoreCase = true)) {
            throw IllegalArgumentException("Invalid `connectionUrl`. It should start with `$NOSTR_CONNECT_PREFIX`.")
        }

        val parsedUrl = Url(urlString = connectionUrl)
        val clientPubKey = parsedUrl.host
        val relays = extractRelaysOrThrow(parsedUrl)
        val secret = extractSecretOrThrow(parsedUrl)
        val perms = extractPermsOrEmpty(parsedUrl)
        val name = parsedUrl.parameters[NAME_PARAM]
        val url = parsedUrl.parameters[URL_PARAM]
        val image = parsedUrl.parameters[IMAGE_PARAM]

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
        url.parameters.getAll(RELAY_PARAM)
            ?: throw IllegalArgumentException(
                "No `$RELAY_PARAM` fields found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractSecretOrThrow(url: Url): String =
        url.parameters[SECRET_PARAM]
            ?: throw IllegalArgumentException(
                "No `$SECRET_PARAM` field found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractPermsOrEmpty(url: Url): List<AppPermission> {
        return emptyList()
    }
}
