package net.primal.android.signer.provider.utils

import android.content.Intent
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.model.LocalSignerMethodResponse

fun LocalSignerMethodResponse.toIntent() =
    Intent().apply {
        putExtra("id", this@toIntent.eventId)
        putExtra("result", this@toIntent.getResultString())

        if (this@toIntent is LocalSignerMethodResponse.Success.SignEvent) {
            putExtra("event", this@toIntent.signedEvent.encodeToJsonString())
        }
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
