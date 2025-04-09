package net.primal.android.scanner.domain

import net.primal.android.scanner.domain.QrCodeDataType.entries
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.wallet.utils.isBitcoinAddress
import net.primal.android.wallet.utils.isBitcoinUri
import net.primal.android.wallet.utils.isLightningUri
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.android.wallet.utils.isLnUrl
import net.primal.domain.nostr.utils.isNProfile
import net.primal.domain.nostr.utils.isNProfileUri
import net.primal.domain.nostr.utils.isNPub
import net.primal.domain.nostr.utils.isNPubUri
import net.primal.domain.nostr.utils.isNote
import net.primal.domain.nostr.utils.isNoteUri

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
    NWC_URL(validator = { it.isNwcUrl() }),
    ;

    companion object {
        fun from(value: String): QrCodeDataType? {
            return entries.firstOrNull { it.validator(value) }
        }
    }
}
