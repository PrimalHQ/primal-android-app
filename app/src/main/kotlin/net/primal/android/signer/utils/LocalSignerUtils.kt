package net.primal.android.signer.utils

import android.content.Intent
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.model.LocalSignerMethodResponse

fun LocalSignerMethodResponse.toIntent() =
    Intent().apply {
        putExtra("id", this@toIntent.eventId)
        putExtra("result", this@toIntent.getResultString())

        if (this@toIntent is LocalSignerMethodResponse.SignEvent) {
            putExtra("event", this@toIntent.signedEvent.encodeToJsonString())
        }
    }

fun LocalSignerMethodResponse.getResultString() =
    when (this) {
        is LocalSignerMethodResponse.DecryptZapEvent -> this.signedEvent.encodeToJsonString()
        is LocalSignerMethodResponse.Nip04Decrypt -> this.plaintext
        is LocalSignerMethodResponse.Nip04Encrypt -> this.ciphertext
        is LocalSignerMethodResponse.Nip44Decrypt -> this.plaintext
        is LocalSignerMethodResponse.Nip44Encrypt -> this.ciphertext
        is LocalSignerMethodResponse.SignEvent -> this.signedEvent.sig
        is LocalSignerMethodResponse.GetPublicKey -> this.pubkey
    }
