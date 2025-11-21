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
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.ConnectionRepository

private const val NOSTR_CONNECT_PREFIX = "nostrconnect://"
private const val NAME_PARAM = "name"
private const val IMAGE_PARAM = "image"
private const val URL_PARAM = "url"
private const val RELAY_PARAM = "relay"
private const val SECRET_PARAM = "secret"
private const val PERMS_PARAM = "perms"

private const val GET_PUBLIC_KEY = "get_public_key"
private const val SIGN_EVENT = "sign_event"
private const val NIP04_DECRYPT = "nip04_decrypt"
private const val NIP44_DECRYPT = "nip44_decrypt"
private const val NIP04_ENCRYPT = "nip04_encrypt"
private const val NIP44_ENCRYPT = "nip44_encrypt"

private const val VALID_PERMISSION_STRING_REGEX =
    "$GET_PUBLIC_KEY|$NIP04_DECRYPT|$NIP04_ENCRYPT|$NIP44_DECRYPT|$NIP44_ENCRYPT|($SIGN_EVENT:\\d+)"

@OptIn(ExperimentalUuidApi::class)
class SignerConnectionInitializer internal constructor(
    private val connectionRepository: ConnectionRepository,
    private val nostrRelayManager: NostrRelayManager,
    private val internalPermissionsRepository: InternalPermissionsRepository,
) {
    suspend fun initialize(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
        trustLevel: TrustLevel,
    ): Result<AppConnection> =
        runCatching {
            val (appConnection, secret) = parseConnectionUrlOrThrow(
                signerPubKey = signerPubKey,
                userPubKey = userPubKey,
                connectionUrl = connectionUrl,
                trustLevel = trustLevel,
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

    private suspend fun parseConnectionUrlOrThrow(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
        trustLevel: TrustLevel,
    ): Pair<AppConnection, String> {
        if (!connectionUrl.startsWith(prefix = NOSTR_CONNECT_PREFIX, ignoreCase = true)) {
            throw IllegalArgumentException("Invalid `connectionUrl`. It should start with `$NOSTR_CONNECT_PREFIX`.")
        }

        val connectionId = Uuid.random().toString()

        val parsedUrl = Url(urlString = connectionUrl)
        val clientPubKey = parsedUrl.host
        val relays = extractRelaysOrThrow(parsedUrl)
        val secret = extractSecretOrThrow(parsedUrl)
        val perms = extractPermsOrEmpty(url = parsedUrl, connectionId = connectionId)
        val name = parsedUrl.parameters[NAME_PARAM]
        val url = parsedUrl.parameters[URL_PARAM]
        val image = parsedUrl.parameters[IMAGE_PARAM]

        val defaultPermissions = if (trustLevel == TrustLevel.Medium) {
            getMediumTrustPermissions(connectionId = connectionId)
        } else {
            emptyList()
        }

        return AppConnection(
            connectionId = connectionId,
            userPubKey = userPubKey,
            signerPubKey = signerPubKey,
            clientPubKey = clientPubKey,
            relays = relays,
            name = name,
            url = url,
            image = image,
            permissions = (perms + defaultPermissions).distinctBy(AppPermission::permissionId),
            autoStart = true,
            trustLevel = trustLevel,
        ) to secret
    }

    private fun extractRelaysOrThrow(url: Url): List<String> =
        url.parameters.getAll(RELAY_PARAM)?.map { it.dropLastWhile { c -> c == '/' } }
            ?: throw IllegalArgumentException(
                "No `$RELAY_PARAM` fields found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractSecretOrThrow(url: Url): String =
        url.parameters[SECRET_PARAM]
            ?: throw IllegalArgumentException(
                "No `$SECRET_PARAM` field found in provided `connectionUrl`. This is a mandatory field.",
            )

    private fun extractPermsOrEmpty(url: Url, connectionId: String): List<AppPermission> {
        return url.parameters[PERMS_PARAM]
            ?.split(",")
            ?.filter { permString -> Regex(VALID_PERMISSION_STRING_REGEX).matches(permString) }
            ?.map {
                AppPermission(
                    permissionId = it,
                    connectionId = connectionId,
                    action = PermissionAction.Approve,
                )
            } ?: emptyList()
    }

    private suspend fun getMediumTrustPermissions(connectionId: String): List<AppPermission> =
        internalPermissionsRepository
            .getMediumTrustPermissions()
            .getOrNull()
            ?.map {
                AppPermission(
                    permissionId = it,
                    connectionId = connectionId,
                    action = PermissionAction.Approve,
                )
            } ?: emptyList()
}
