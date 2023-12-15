package net.primal.android.scan.analysis

enum class QrCodeDataType(val validator: (String) -> Boolean) {
    NPUB(validator = { it.startsWith(prefix = "nostr:npub1", ignoreCase = true) }),

    NOTE(validator = { it.startsWith(prefix = "nostr:note1", ignoreCase = true) }),

    NEVENT(validator = { it.startsWith(prefix = "nostr:nevent1", ignoreCase = true) }),

    LNBC(validator = { it.startsWith(prefix = "lnbc", ignoreCase = true) }),

    LNURL(validator = { it.startsWith(prefix = "lnurl", ignoreCase = true) }),

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
)
