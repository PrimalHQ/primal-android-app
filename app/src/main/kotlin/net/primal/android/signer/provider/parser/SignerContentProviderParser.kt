package net.primal.android.signer.provider.parser

import android.net.Uri
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import net.primal.android.signer.model.SignerMethod
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.signer.local.LocalSignerMethod

class SignerContentProviderParser {
    fun parse(
        uri: Uri,
        params: List<String>,
        callingPackage: String?,
    ): Result<LocalSignerMethod> =
        runCatching {
            requireNotNull(callingPackage) { "Calling package must not be null." }

            val method = uri.host?.split(".")?.lastOrNull()
                ?.lowercase()?.let { SignerMethod.fromString(it) }
                ?: throw IllegalArgumentException("Couldn't parse method name from Uri: $uri.")

            when (method) {
                SignerMethod.GET_PUBLIC_KEY -> error(
                    "Received `${SignerMethod.GET_PUBLIC_KEY}` in ContentProvider. Unable to process this here.",
                )

                SignerMethod.SIGN_EVENT -> signEvent(params, callingPackage)
                SignerMethod.NIP04_DECRYPT -> nip04Decrypt(params, callingPackage)
                SignerMethod.NIP04_ENCRYPT -> nip04Encrypt(params, callingPackage)
                SignerMethod.NIP44_DECRYPT -> nip44Decrypt(params, callingPackage)
                SignerMethod.NIP44_ENCRYPT -> nip44Encrypt(params, callingPackage)
                SignerMethod.DECRYPT_ZAP_EVENT -> decryptZapEvent(params, callingPackage)
            }
        }

    private fun decryptZapEvent(params: List<String>, callingPackage: String): LocalSignerMethod =
        LocalSignerMethod.DecryptZapEvent(
            eventId = Uuid.random().toString(),
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = params[2],
            signedEvent = requireNotNull(
                params[0].decodeFromJsonStringOrNull(),
            ) { "Couldn't decode nostr event json." },
        )

    private fun nip44Encrypt(params: List<String>, callingPackage: String): LocalSignerMethod =
        LocalSignerMethod.Nip44Encrypt(
            eventId = Uuid.random().toString(),
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = params[2],
            otherPubKey = params[1],
            plaintext = params[0],
        )

    private fun nip44Decrypt(params: List<String>, callingPackage: String): LocalSignerMethod =
        LocalSignerMethod.Nip44Decrypt(
            eventId = Uuid.random().toString(),
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = params[2],
            otherPubKey = params[1],
            ciphertext = params[0],
        )

    private fun nip04Encrypt(params: List<String>, callingPackage: String): LocalSignerMethod =
        LocalSignerMethod.Nip04Encrypt(
            eventId = Uuid.random().toString(),
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = params[2],
            otherPubKey = params[1],
            plaintext = params[0],
        )

    private fun nip04Decrypt(params: List<String>, callingPackage: String): LocalSignerMethod =
        LocalSignerMethod.Nip04Decrypt(
            eventId = Uuid.random().toString(),
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = params[2],
            otherPubKey = params[1],
            ciphertext = params[0],
        )

    private fun signEvent(params: List<String>, callingPackage: String): LocalSignerMethod =
        LocalSignerMethod.SignEvent(
            eventId = Uuid.random().toString(),
            packageName = callingPackage,
            requestedAt = Clock.System.now().epochSeconds,
            userPubKey = params[2],
            unsignedEvent = requireNotNull(
                params[0].decodeFromJsonStringOrNull(),
            ) { "Couldn't decode unsigned event json." },
        )
}
