package net.primal.android.signer.provider.parser

import android.content.Intent
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import net.primal.android.signer.model.Permission
import net.primal.android.signer.model.SignerMethod
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

class SignerIntentParser {
    companion object {
        private const val NOSTR_SIGNER_SCHEME = "nostrsigner"

        const val TYPE_COLUMN = "type"
        const val PERMISSIONS_COLUMN = "permissions"
        const val ID_COLUMN = "id"
        const val CURRENT_USER_COLUMN = "current_user"
        const val PUBKEY_COLUMN = "pubkey"
    }

    fun parse(intent: Intent, callingPackage: String?): Result<LocalSignerMethod> =
        runCatching {
            requireNotNull(callingPackage) { "Calling package was null." }
            require(intent.scheme == NOSTR_SIGNER_SCHEME) {
                "Given Intents scheme is not $NOSTR_SIGNER_SCHEME. Is it a signer intent?"
            }
            val type = requireNotNull(intent.getStringExtra(TYPE_COLUMN)) {
                "There is no column `type` in received intent."
            }

            when (type) {
                SignerMethod.GET_PUBLIC_KEY.method -> parseAsGetPublicKey(intent, callingPackage)
                SignerMethod.SIGN_EVENT.method -> parseAsSignEvent(intent, callingPackage)
                SignerMethod.NIP04_ENCRYPT.method -> parseAsNip04Encrypt(intent, callingPackage)
                SignerMethod.NIP44_ENCRYPT.method -> parseAsNip44Encrypt(intent, callingPackage)
                SignerMethod.NIP04_DECRYPT.method -> parseAsNip04Decrypt(intent, callingPackage)
                SignerMethod.NIP44_DECRYPT.method -> parseAsNip44Decrypt(intent, callingPackage)
                SignerMethod.DECRYPT_ZAP_EVENT.method -> parseAsDecryptZapEvent(intent, callingPackage)

                else -> error("Unexpected method type. Received $type.")
            }
        }

    private fun parseAsGetPublicKey(intent: Intent, callingPackage: String): LocalSignerMethod {
        val permissions = (
            intent.getStringExtra(PERMISSIONS_COLUMN)
                .decodeFromJsonStringOrNull<List<Permission>>()
                ?: emptyList()
            )
            .map {
                AppPermission(
                    permissionId = if (it.type == SignerMethod.SIGN_EVENT) {
                        "${it.type.method}:${it.kind}"
                    } else {
                        it.type.method
                    },
                    clientPubKey = "",
                    action = AppPermissionAction.Ask,
                )
            }

        return LocalSignerMethod.GetPublicKey(
            eventId = Uuid.random().toString(),
            permissions = permissions,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
        )
    }

    private fun parseAsSignEvent(intent: Intent, callingPackage: String): LocalSignerMethod {
        val (id, currentUser) = intent.extractColumnsOrThrow(ID_COLUMN, CURRENT_USER_COLUMN)

        val event = intent.getUriPayload().decodeFromJsonStringOrNull<NostrUnsignedEvent>()

        requireNotNull(event) { "Missing required `event` argument for `sign_event`." }

        return LocalSignerMethod.SignEvent(
            eventId = id,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = currentUser,
            unsignedEvent = event,
        )
    }

    private fun parseAsNip04Encrypt(intent: Intent, callingPackage: String): LocalSignerMethod {
        val (id, currentUser, pubkey) = intent.extractColumnsOrThrow(ID_COLUMN, CURRENT_USER_COLUMN, PUBKEY_COLUMN)

        val plaintext = requireNotNull(intent.getUriPayload()) {
            "Missing required `plaintext` argument for `${SignerMethod.NIP04_ENCRYPT.method}`."
        }

        return LocalSignerMethod.Nip04Encrypt(
            eventId = id,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = currentUser,
            otherPubKey = pubkey,
            plaintext = plaintext,
        )
    }

    private fun parseAsNip44Encrypt(intent: Intent, callingPackage: String): LocalSignerMethod {
        val (id, currentUser, pubkey) = intent.extractColumnsOrThrow(ID_COLUMN, CURRENT_USER_COLUMN, PUBKEY_COLUMN)

        val plaintext = requireNotNull(intent.getUriPayload()) {
            "Missing required `plaintext` argument for `${SignerMethod.NIP44_ENCRYPT.method}`."
        }

        return LocalSignerMethod.Nip44Encrypt(
            eventId = id,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = currentUser,
            otherPubKey = pubkey,
            plaintext = plaintext,
        )
    }

    private fun parseAsNip04Decrypt(intent: Intent, callingPackage: String): LocalSignerMethod {
        val (id, currentUser, pubkey) = intent.extractColumnsOrThrow(ID_COLUMN, CURRENT_USER_COLUMN, PUBKEY_COLUMN)

        val ciphertext = requireNotNull(intent.getUriPayload()) {
            "Missing required `ciphertext` argument for `${SignerMethod.NIP04_DECRYPT.method}`."
        }

        return LocalSignerMethod.Nip04Decrypt(
            eventId = id,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = currentUser,
            otherPubKey = pubkey,
            ciphertext = ciphertext,
        )
    }

    private fun parseAsNip44Decrypt(intent: Intent, callingPackage: String): LocalSignerMethod {
        val (id, currentUser, pubkey) = intent.extractColumnsOrThrow(ID_COLUMN, CURRENT_USER_COLUMN, PUBKEY_COLUMN)

        val ciphertext = requireNotNull(intent.getUriPayload()) {
            "Missing required `ciphertext` argument for `${SignerMethod.NIP44_DECRYPT.method}`."
        }

        return LocalSignerMethod.Nip44Decrypt(
            eventId = id,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = currentUser,
            otherPubKey = pubkey,
            ciphertext = ciphertext,
        )
    }

    private fun parseAsDecryptZapEvent(intent: Intent, callingPackage: String): LocalSignerMethod {
        val (id, currentUser) = intent.extractColumnsOrThrow(ID_COLUMN, CURRENT_USER_COLUMN)
        val event = requireNotNull(intent.getUriPayload().decodeFromJsonStringOrNull<NostrEvent>()) {
            "Missing required `event` argument in `${SignerMethod.DECRYPT_ZAP_EVENT.method}`."
        }

        return LocalSignerMethod.DecryptZapEvent(
            eventId = id,
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = currentUser,
            signedEvent = event,
        )
    }

    private fun Intent.getUriPayload() = data?.toString()?.removePrefix("$NOSTR_SIGNER_SCHEME:")

    private fun Intent.extractColumnsOrThrow(vararg columns: String): List<String> {
        return columns.map { column ->
            requireNotNull(getStringExtra(column)) {
                "Missing required argument $column."
            }
        }
    }
}
