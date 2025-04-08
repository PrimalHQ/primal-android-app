package net.primal.android.core.utils

import io.github.aakira.napier.Napier
import net.primal.domain.nostr.cryptography.utils.Bech32
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp

private val HEXADECIMAL_PATTERN = Regex("\\p{XDigit}+")

fun String.isValidHex() = HEXADECIMAL_PATTERN.matches(this)

fun String?.isValidNostrPrivateKey(): Boolean {
    if (this == null) return false

    return if (startsWith("nsec")) {
        this.isValidNsec()
    } else {
        try {
            this.hexToNsecHrp().isValidNsec()
        } catch (error: IllegalArgumentException) {
            Napier.w(error) { error.message ?: "" }
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
        } catch (error: IllegalArgumentException) {
            Napier.w(error) { error.message ?: "" }
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
        Napier.w(error) { error.message ?: "" }
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
        Napier.w(error) { error.message ?: "" }
        false
    }
}
