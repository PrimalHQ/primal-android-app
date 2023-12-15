package net.primal.android.scan.analysis

import android.util.Patterns

enum class QrCodeDataType(val validator: (String) -> Boolean) {
    NPUB(validator = { it.startsWith(prefix = "nostr:npub1", ignoreCase = true) }),

    NOTE(validator = { it.startsWith(prefix = "nostr:note1", ignoreCase = true) }),

    NEVENT(validator = { it.startsWith(prefix = "nostr:nevent1", ignoreCase = true) }),

    LNBC(validator = { it.startsWith(prefix = "lnbc", ignoreCase = true) }),

    LNURL(validator = { it.startsWith(prefix = "lnurl", ignoreCase = true) }),

    LUD16(
        validator = {
            it.startsWith(prefix = "lightning:", ignoreCase = true) &&
                Patterns.EMAIL_ADDRESS.matcher(it.split(":").last()).matches()
        },
    ),

    ;

    companion object {
        fun from(value: String): QrCodeDataType? {
            return entries.firstOrNull { it.validator(value) }
        }
    }
}

data class QrCodeResult(
    val value: String,
    val type: QrCodeDataType,
) {
    fun equalValues(other: QrCodeResult?): Boolean = this.value == other?.value
}
