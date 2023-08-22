package net.primal.android.core.utils

import net.primal.android.crypto.Bech32
import net.primal.android.crypto.hexToNsecHrp
import org.spongycastle.util.encoders.DecoderException

fun String?.isValidNostrKey(): Boolean {
    if (this == null) return false

    return if (startsWith("nsec")) {
        this.isValidNsec()
    } else {
        try {
            this.hexToNsecHrp().isValidNsec()
        } catch (error: DecoderException) {
            false
        }
    }
}

private fun String.isValidNsec(): Boolean {
    if (!this.startsWith("nsec")) return false

    return try {
        val decoded = Bech32.decodeBytes(this)
        decoded.first == "nsec" && decoded.second.size == 32
    } catch (error: IllegalArgumentException) {
        false
    }
}

