package net.primal.android.scanner.domain

import net.primal.android.nostr.ext.isNProfile
import net.primal.android.nostr.ext.isNProfileUri
import net.primal.android.nostr.ext.isNPub
import net.primal.android.nostr.ext.isNPubUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.isNoteUri
import net.primal.android.wallet.utils.isBitcoinAddress
import net.primal.android.wallet.utils.isBitcoinUri
import net.primal.android.wallet.utils.isLightningUri
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.android.wallet.utils.isLnUrl

enum class QrCodeDataType(val validator: (String) -> Boolean) {
    NPUB_URI(validator = { it.isNPubUri() }),
    NPUB(validator = { it.isNPub() }),
    NPROFILE_URI(validator = { it.isNProfileUri() }),
    NPROFILE(validator = { it.isNProfile() }),
    NOTE_URI(validator = { it.isNoteUri() }),
    NOTE(validator = { it.isNote() }),
    LIGHTNING_URI(validator = { it.isLightningUri() }),
    LNBC(validator = { it.isLnInvoice() }),
    LNURL(validator = { it.isLnUrl() }),
    BITCOIN_URI(validator = { it.isBitcoinUri() }),
    BITCOIN_ADDRESS(validator = { it.isBitcoinAddress() }),
    ;

    companion object {
        fun from(value: String): QrCodeDataType? {
            return entries.firstOrNull { it.validator(value) }
        }
    }
}
