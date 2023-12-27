package net.primal.android.scanner.analysis

import net.primal.android.wallet.utils.isLightningAddressUri
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.android.wallet.utils.isLnUrl

enum class QrCodeDataType(val validator: (String) -> Boolean) {
    NPUB(validator = { it.startsWith(prefix = "nostr:npub1", ignoreCase = true) }),

    NOTE(validator = { it.startsWith(prefix = "nostr:note1", ignoreCase = true) }),

    NEVENT(validator = { it.startsWith(prefix = "nostr:nevent1", ignoreCase = true) }),

    LNBC(validator = { it.isLnInvoice() }),

    LNURL(validator = { it.isLnUrl() }),

    LUD16(validator = { it.isLightningAddressUri() }),

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
