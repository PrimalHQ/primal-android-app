package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.wallet.send.create.DraftTransaction
import net.primal.android.wallet.send.prepare.tabs.SendPaymentTab

const val FEED_DIRECTIVE = "directive"
inline val SavedStateHandle.feedDirective: String?
    get() = get<String>(
        FEED_DIRECTIVE,
    )?.asUrlDecoded()

const val NOTE_ID = "noteId"
inline val SavedStateHandle.noteIdOrThrow: String
    get() = get(NOTE_ID) ?: throw IllegalArgumentException("Missing required noteId argument.")

const val PROFILE_ID = "profileId"
inline val SavedStateHandle.profileId: String? get() = get(PROFILE_ID)
inline val SavedStateHandle.profileIdOrThrow: String
    get() = get(PROFILE_ID) ?: throw IllegalArgumentException("Missing required profileId argument")

const val SEARCH_QUERY = "searchQuery"
inline val SavedStateHandle.searchQueryOrThrow: String
    get() = get(SEARCH_QUERY) ?: throw IllegalArgumentException("Missing required searchQuery argument.")

const val NEW_POST_PRE_FILL_CONTENT = "preFillContent"
inline val SavedStateHandle.newPostPreFillContent: String?
    get() = get<String>(NEW_POST_PRE_FILL_CONTENT)?.asBase64Decoded()?.ifEmpty { null }

const val NEW_POST_PRE_FILL_FILE_URI = "preFillFileUri"
inline val SavedStateHandle.newPostPreFillFileUri: String?
    get() = get<String?>(NEW_POST_PRE_FILL_FILE_URI)?.asUrlDecoded()?.ifEmpty { null }

const val NEW_POST_REPLY_TO_NOTE_ID = "replyToNoteId"
inline val SavedStateHandle.replyToNoteId: String?
    get() = get<String?>(NEW_POST_REPLY_TO_NOTE_ID)?.ifEmpty { null }

const val NWC_URL = "nwcUrl"
inline val SavedStateHandle.nwcUrl: String? get() = get(NWC_URL)

const val MEDIA_URL = "mediaUrl"
inline val SavedStateHandle.mediaUrl: String? get() = get(MEDIA_URL)

const val SEND_PAYMENT_TAB = "sendPaymentTab"
inline val SavedStateHandle.sendPaymentTab: SendPaymentTab?
    get() = get<String?>(SEND_PAYMENT_TAB)?.let {
        SendPaymentTab.valueOf(it)
    }

const val DRAFT_TRANSACTION = "draftTransaction"
inline val SavedStateHandle.draftTransaction: DraftTransaction
    get() = get<String>(DRAFT_TRANSACTION)
        ?.asBase64Decoded()?.let {
            NostrJson.decodeFromString(it)
        } ?: throw IllegalArgumentException("Missing draft transaction.")
