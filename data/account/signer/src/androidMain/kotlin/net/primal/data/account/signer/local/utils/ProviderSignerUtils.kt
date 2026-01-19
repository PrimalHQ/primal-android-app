package net.primal.data.account.signer.local.utils

import android.content.Intent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.core.utils.getIfTypeOrNull
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

fun List<LocalSignerMethodResponse>.toIntent(packageName: String) =
    Intent().apply {
        val results = this@toIntent.asSignerResults(packageName)
        putExtra("results", results.encodeToJsonString())
    }

private fun List<LocalSignerMethodResponse>.asSignerResults(packageName: String) =
    map { response ->
        when (response) {
            is LocalSignerMethodResponse.Error -> {
                SignerResult.Error(
                    id = response.eventId,
                    packageName = packageName,
                    error = response.message,
                )
            }

            is LocalSignerMethodResponse.Success -> {
                SignerResult.Success(
                    id = response.eventId,
                    result = response.getResultString(),
                    packageName = packageName,
                    event = response
                        .getIfTypeOrNull(LocalSignerMethodResponse.Success.SignEvent::signedEvent)
                        ?.encodeToJsonString(),
                )
            }
        }
    }

@SerialName("Result")
@Serializable
private sealed class SignerResult() {
    abstract val id: String

    @SerialName("package")
    abstract val packageName: String

    @Serializable
    data class Success(
        override val id: String,
        override val packageName: String,
        val event: String? = null,
        val result: String,
    ) : SignerResult()

    @Serializable
    data class Error(
        override val id: String,
        override val packageName: String,
        val error: String,
    ) : SignerResult()
}

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
