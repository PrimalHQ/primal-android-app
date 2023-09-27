package net.primal.android.core.utils

import net.primal.android.crypto.Bech32
import net.primal.android.crypto.hexToNsecHrp
import org.spongycastle.util.encoders.DecoderException

fun String?.isValidNostrPrivateKey(): Boolean {
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

fun String?.isValidNostrPublicKey(): Boolean {
    if (this == null) return false

    return if (startsWith("npub")) {
        this.isValidNpub()
    } else {
        try {
            this.hexToNsecHrp().isValidNpub()
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

private fun String.isValidNpub(): Boolean {
    if (!this.startsWith("npub")) return false

    return try {
        val decoded = Bech32.decodeBytes(this)
        decoded.first == "npub" && this.length >= 64
    } catch (error: IllegalArgumentException) {
        false
    }
}

fun String.isValidUsername(): Boolean = all { it.isLetterOrDigit() }
