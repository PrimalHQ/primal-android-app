package net.primal.android.core.utils

import android.util.Patterns
import java.util.regex.Pattern
import net.primal.android.crypto.Bech32
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.crypto.hexToNsecHrp
import org.spongycastle.util.encoders.DecoderException
import timber.log.Timber

private val HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+")

fun String.isValidHex() = HEXADECIMAL_PATTERN.matcher(this).matches()

fun String?.isValidNostrPrivateKey(): Boolean {
    if (this == null) return false

    return if (startsWith("nsec")) {
        this.isValidNsec()
    } else {
        try {
            this.hexToNsecHrp().isValidNsec()
        } catch (error: DecoderException) {
            Timber.w(error)
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
            this.hexToNpubHrp().isValidNpub()
        } catch (error: DecoderException) {
            Timber.w(error)
            false
        }
    }
}

private fun String.isValidNsec(): Boolean {
    if (!this.startsWith("nsec")) return false

    return try {
        val decoded = Bech32.decodeBytes(this)
        decoded.first == "nsec" && decoded.second.size == KEY_BYTES_SIZE
    } catch (error: IllegalArgumentException) {
        Timber.w(error)
        false
    }
}

private const val NPUB_LENGTH = 63
private const val KEY_BYTES_SIZE = 32

private fun String.isValidNpub(): Boolean {
    if (!this.startsWith("npub")) return false

    return try {
        val decoded = Bech32.decodeBytes(this)
        decoded.first == "npub" && this.length >= NPUB_LENGTH
    } catch (error: IllegalArgumentException) {
        Timber.w(error)
        false
    }
}

fun String.isValidUsername(): Boolean = all { it.isLetterOrDigit() }

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
