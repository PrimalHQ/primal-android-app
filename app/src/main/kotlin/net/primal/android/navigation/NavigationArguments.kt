package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.explore.asearch.AdvancedSearchContract
import net.primal.android.explore.search.ui.SearchScope
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

const val RENDER_TYPE = "renderType"
inline val SavedStateHandle.renderType: String
    get() = get<String>(RENDER_TYPE) ?: throw IllegalArgumentException("Missing required renderType argument.")

const val EXTEND_EXISTING_PREMIUM_NAME = "extendExistingPremiumName"
inline val SavedStateHandle.extendExistingPremiumName: String? get() = get(EXTEND_EXISTING_PREMIUM_NAME)

const val FROM_ORIGIN_PREMIUM_BADGE = "OriginPremiumBadge"
const val FROM_ORIGIN = "buyingPremiumFromOrigin"
inline val SavedStateHandle.buyingPremiumFromOrigin: String? get() = get(FROM_ORIGIN)
inline val NavBackStackEntry.buyingPremiumFromOrigin: String? get() = arguments?.getString(FROM_ORIGIN)

const val INITIAL_QUERY = "initialQuery"
inline val SavedStateHandle.initialQuery: String? get() = get(INITIAL_QUERY)

const val POSTED_BY = "postedBy"
inline val SavedStateHandle.postedBy: List<String>?
    get() = get<String>(POSTED_BY)?.let {
        NostrJson.decodeFromStringOrNull(it)
    }

const val SEARCH_KIND = "searchKind"
inline val SavedStateHandle.searchKind: AdvancedSearchContract.SearchKind?
    get() = get<String>(SEARCH_KIND)?.let {
        AdvancedSearchContract.SearchKind.valueOf(it)
    }

const val ADV_SEARCH_SCOPE = "advSearchScope"
inline val SavedStateHandle.advSearchScope: AdvancedSearchContract.SearchScope?
    get() = get<String>(ADV_SEARCH_SCOPE)?.let {
        AdvancedSearchContract.SearchScope.valueOf(it)
    }

const val PREMIUM_MORE_INFO_TAB_INDEX = "premiumMoreInfoTab"
inline val NavBackStackEntry.premiumMoreInfoTabIndex: Int?
    get() = arguments?.getInt(PREMIUM_MORE_INFO_TAB_INDEX)

const val SEARCH_SCOPE = "searchScope"
inline val NavBackStackEntry.searchScopeOrThrow: SearchScope
    get() = arguments?.getString(SEARCH_SCOPE)?.let { SearchScope.valueOf(it) }
        ?: throw IllegalArgumentException("Missing required searchScope argument.")

const val EXPLORE_FEED_SPEC = "exploreFeedSpec"
inline val SavedStateHandle.exploreFeedSpecOrThrow: String
    get() = get<String>(EXPLORE_FEED_SPEC)?.asBase64Decoded()?.ifEmpty { null }
        ?: throw IllegalArgumentException("Missing required exploreFeedSpec argument.")

const val NOTE_EDITOR_ARGS = "preFillContent"

const val NWC_APP_NAME = "appName"
inline val SavedStateHandle.appName: String? get() = get(NWC_APP_NAME)

const val NWC_APP_ICON = "appIcon"
inline val SavedStateHandle.appIcon: String? get() = get(NWC_APP_ICON)

const val NWC_CALLBACK = "callback"
inline val SavedStateHandle.callback: String
    get() = get(NWC_CALLBACK) ?: throw IllegalArgumentException("Missing required $NWC_CALLBACK argument.")

const val MEDIA_URL = "mediaUrl"
inline val SavedStateHandle.mediaUrl: String? get() = get(MEDIA_URL)
inline val SavedStateHandle.mediaUrlOrThrow: String
    get() = get(MEDIA_URL) ?: throw IllegalArgumentException("Missing required $MEDIA_URL argument.")

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
            NostrJson.decodeFromStringOrNull(it)
        } ?: throw IllegalArgumentException("Missing draft transaction.")

const val LNBC = "lnbc"
inline val SavedStateHandle.lnbc: String? get() = get<String>(LNBC)

const val TRANSACTION_ID = "transactionId"
inline val SavedStateHandle.transactionIdOrThrow: String
    get() = get(TRANSACTION_ID) ?: throw IllegalArgumentException("Missing required transactionId argument.")
