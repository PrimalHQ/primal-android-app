package net.primal.android.core.utils

import net.primal.android.crypto.Bech32

fun String?.isValidNsec(): Boolean {
    if (this == null || !this.startsWith("nsec")) return false

    return try {
        val decoded = Bech32.decodeBytes(this)
        decoded.first == "nsec" && decoded.second.isNotEmpty()
    } catch (error: IllegalArgumentException) {
        false
    }
}
