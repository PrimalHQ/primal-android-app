package net.primal.android.scanner.domain

import net.primal.android.redeem.utils.isNostrConnectUrl // <-- DODAJ OVAJ IMPORT
import net.primal.android.redeem.utils.isPromoCodeUrl
import net.primal.domain.nostr.utils.isNAddr
import net.primal.domain.nostr.utils.isNAddrUri
import net.primal.domain.nostr.utils.isNEvent
import net.primal.domain.nostr.utils.isNEventUri
import net.primal.domain.nostr.utils.isNProfile
import net.primal.domain.nostr.utils.isNProfileUri
import net.primal.domain.nostr.utils.isNPub
import net.primal.domain.nostr.utils.isNPubUri
import net.primal.domain.nostr.utils.isNote
import net.primal.domain.nostr.utils.isNoteUri
import net.primal.domain.parser.isNwcUrl
import net.primal.domain.utils.isBitcoinAddress
import net.primal.domain.utils.isBitcoinUri
import net.primal.domain.utils.isLightningUri
import net.primal.domain.utils.isLnInvoice
import net.primal.domain.utils.isLnUrl

enum class QrCodeDataType(val validator: (String) -> Boolean) {
    NPUB_URI(validator = { it.isNPubUri() }),
    NPUB(validator = { it.isNPub() }),
    NPROFILE_URI(validator = { it.isNProfileUri() }),
    NPROFILE(validator = { it.isNProfile() }),
    NADDR_URI(validator = { it.isNAddrUri() }),
    NADDR(validator = { it.isNAddr() }),
    NEVENT_URI(validator = { it.isNEventUri() }),
    NEVENT(validator = { it.isNEvent() }),
    NOTE_URI(validator = { it.isNoteUri() }),
    NOTE(validator = { it.isNote() }),
    LIGHTNING_URI(validator = { it.isLightningUri() }),
    LNBC(validator = { it.isLnInvoice() }),
    LNURL(validator = { it.isLnUrl() }),
    BITCOIN_URI(validator = { it.isBitcoinUri() }),
    BITCOIN_ADDRESS(validator = { it.isBitcoinAddress() }),
    NWC_URL(validator = { it.isNwcUrl() }),
    NOSTR_CONNECT(validator = { it.isNostrConnectUrl() }),
    PROMO_CODE(validator = { it.isPromoCodeUrl() }),
    ;

    companion object {
        fun from(value: String): QrCodeDataType? {
            return entries.firstOrNull { it.validator(value) }
        }
    }
}
