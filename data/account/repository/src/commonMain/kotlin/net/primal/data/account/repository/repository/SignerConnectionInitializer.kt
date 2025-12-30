package net.primal.data.account.repository.repository

import io.ktor.http.Url
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.utils.Result
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.local.dao.apps.SignerMethodType
import net.primal.data.account.repository.repository.internal.InternalPermissionsRepository
import net.primal.data.account.repository.repository.internal.InternalRemoteSessionEventRepository
import net.primal.data.account.signer.remote.model.RemoteSignerMethodResponse
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.RemoteAppConnection
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

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
    private val sessionRepository: SessionRepository,
    private val internalSessionEventRepository: InternalRemoteSessionEventRepository,
    private val internalPermissionsRepository: InternalPermissionsRepository,
) {
    suspend fun initialize(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
        trustLevel: TrustLevel,
        nwcConnectionString: String? = null,
    ): Result<RemoteAppConnection> =
        runCatching {
            val (appConnection, secret) = parseConnectionUrlOrThrow(
                signerPubKey = signerPubKey,
                userPubKey = userPubKey,
                connectionUrl = connectionUrl,
                trustLevel = trustLevel,
            )

            val resultPayload = if (nwcConnectionString != null) {
                listOf(secret, nwcConnectionString).encodeToJsonString()
            } else {
                secret
            }

            connectionRepository.insertOrReplaceConnection(secret = secret, connection = appConnection)
            sessionRepository.startRemoteSession(appIdentifier = appConnection.clientPubKey)
                .onSuccess { sessionId ->
                    internalSessionEventRepository.saveRemoteAppSessionEvent(
                        sessionId = sessionId,
                        signerPubKey = signerPubKey,
                        requestType = SignerMethodType.Connect,
                        method = null,
                        requestState = AppRequestState.PendingResponse,
                        response = RemoteSignerMethodResponse.Success(
                            id = Uuid.random().toString(),
                            clientPubKey = appConnection.clientPubKey,
                            result = resultPayload,
                        ),
                    )
                }

            appConnection
        }

    private suspend fun parseConnectionUrlOrThrow(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
        trustLevel: TrustLevel,
    ): Pair<RemoteAppConnection, String> {
        if (!connectionUrl.startsWith(prefix = NOSTR_CONNECT_PREFIX, ignoreCase = true)) {
            throw IllegalArgumentException("Invalid `connectionUrl`. It should start with `$NOSTR_CONNECT_PREFIX`.")
        }

        val parsedUrl = Url(urlString = connectionUrl)
        val clientPubKey = parsedUrl.host
        val relays = extractRelaysOrThrow(parsedUrl)
        val secret = extractSecretOrThrow(parsedUrl)
        val perms = extractPermsOrEmpty(url = parsedUrl, clientPubKey = clientPubKey)
        val name = parsedUrl.parameters[NAME_PARAM]
        val url = parsedUrl.parameters[URL_PARAM]
        val image = parsedUrl.parameters[IMAGE_PARAM]

        val defaultPermissions = if (trustLevel == TrustLevel.Medium) {
            getMediumTrustPermissions(clientPubKey = clientPubKey)
        } else {
            emptyList()
        }

        val defaultPermsIds = defaultPermissions.map { it.permissionId }.toSet()
        val finalPermissions = defaultPermissions + perms.filter { it.permissionId !in defaultPermsIds }

        return RemoteAppConnection(
            userPubKey = userPubKey,
            signerPubKey = signerPubKey,
            clientPubKey = clientPubKey,
            relays = relays,
            name = name,
            url = url,
            image = image,
            permissions = finalPermissions,
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

    private fun extractPermsOrEmpty(url: Url, clientPubKey: String): List<AppPermission> {
        return url.parameters[PERMS_PARAM]
            ?.split(",")
            ?.filter { permString -> Regex(VALID_PERMISSION_STRING_REGEX).matches(permString) }
            ?.map {
                AppPermission(
                    permissionId = it,
                    appIdentifier = clientPubKey,
                    action = AppPermissionAction.Ask,
                )
            } ?: emptyList()
    }

    private suspend fun getMediumTrustPermissions(clientPubKey: String): List<AppPermission> =
        internalPermissionsRepository
            .getMediumTrustPermissions()
            .getOrNull()
            ?.map {
                AppPermission(
                    permissionId = it,
                    appIdentifier = clientPubKey,
                    action = AppPermissionAction.Approve,
                )
            } ?: emptyList()
}
