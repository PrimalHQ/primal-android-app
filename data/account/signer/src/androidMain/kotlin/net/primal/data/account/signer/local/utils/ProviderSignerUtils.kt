package net.primal.data.account.signer.local.utils

import android.content.Intent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse

fun LocalSignerMethodResponse.toIntent() =
    Intent().apply {
        putExtra("id", this@toIntent.eventId)
        putExtra("result", this@toIntent.getResultString())

        if (this@toIntent is LocalSignerMethodResponse.Success.SignEvent) {
            putExtra("event", this@toIntent.signedEvent.encodeToJsonString())
        }
    }

fun List<LocalSignerMethodResponse>.toIntent() =
    Intent().apply {
        val results = this@toIntent.map { methodResponse ->
            SignerResult(
                id = methodResponse.eventId,
                event = if (methodResponse is LocalSignerMethodResponse.Success.SignEvent) {
                    methodResponse.signedEvent.encodeToJsonString()
                } else {
                    null
                },
                result = methodResponse.getResultString(),
            )
        }
        putExtra("results", results.encodeToJsonString())
    }

@Serializable
private data class SignerResult(
    @SerialName("id") val id: String,
    @SerialName("event") val event: String?,
    @SerialName("result") val result: String,
)

fun LocalSignerMethodResponse.getResultString() =
    when (this) {
        is LocalSignerMethodResponse.Success.DecryptZapEvent -> this.signedEvent.encodeToJsonString()
        is LocalSignerMethodResponse.Success.Nip04Decrypt -> this.plaintext
        is LocalSignerMethodResponse.Success.Nip04Encrypt -> this.ciphertext
        is LocalSignerMethodResponse.Success.Nip44Decrypt -> this.plaintext
        is LocalSignerMethodResponse.Success.Nip44Encrypt -> this.ciphertext
        is LocalSignerMethodResponse.Success.SignEvent -> this.signedEvent.sig
        is LocalSignerMethodResponse.Success.GetPublicKey -> this.pubkey
        is LocalSignerMethodResponse.Error -> this.message
    }
