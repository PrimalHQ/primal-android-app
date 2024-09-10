package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab

const val NOTE_ID = "noteId"
inline val SavedStateHandle.noteIdOrThrow: String
    get() = get(NOTE_ID) ?: throw IllegalArgumentException("Missing required noteId argument.")

const val PROFILE_ID = "profileId"
inline val SavedStateHandle.profileId: String? get() = get(PROFILE_ID)
inline val SavedStateHandle.profileIdOrThrow: String
    get() = get(PROFILE_ID) ?: throw IllegalArgumentException("Missing required profileId argument")

const val NADDR = "naddr"
inline val SavedStateHandle.naddrOrThrow: String
    get() = get(NADDR) ?: throw IllegalArgumentException("Missing required naddr argument.")

const val FOLLOWS_TYPE = "followsType"
inline val SavedStateHandle.followsType: String? get() = get(FOLLOWS_TYPE)

const val EXPLORE_FEED_SPEC = "exploreFeedSpec"
inline val SavedStateHandle.exploreFeedSpecOrThrow: String
    get() = get<String>(EXPLORE_FEED_SPEC)?.asBase64Decoded()?.ifEmpty { null }
        ?: throw IllegalArgumentException("Missing required exploreFeedSpec argument.")

const val NOTE_EDITOR_ARGS = "preFillContent"

const val NWC_URL = "nwcUrl"
inline val SavedStateHandle.nwcUrl: String? get() = get(NWC_URL)

const val MEDIA_URL = "mediaUrl"
inline val SavedStateHandle.mediaUrl: String? get() = get(MEDIA_URL)

const val MEDIA_POSITION_MS = "mediaPositionMs"
inline val SavedStateHandle.mediaPositionMs: Long get() = get(MEDIA_POSITION_MS) ?: 0L

const val SEND_PAYMENT_TAB = "sendPaymentTab"
inline val SavedStateHandle.sendPaymentTab: SendPaymentTab?
    get() = get<String?>(SEND_PAYMENT_TAB)?.let {
        SendPaymentTab.valueOf(it)
    }

const val DRAFT_TRANSACTION = "draftTransaction"
inline val SavedStateHandle.draftTransaction: DraftTx
    get() = get<String>(DRAFT_TRANSACTION)
        ?.asBase64Decoded()?.let {
            NostrJson.decodeFromString(it)
        } ?: throw IllegalArgumentException("Missing draft transaction.")

const val LNBC = "lnbc"
inline val SavedStateHandle.lnbc: String? get() = get<String>(LNBC)

const val TRANSACTION_ID = "transactionId"
inline val SavedStateHandle.transactionIdOrThrow: String
    get() = get(TRANSACTION_ID) ?: throw IllegalArgumentException("Missing required transactionId argument.")
