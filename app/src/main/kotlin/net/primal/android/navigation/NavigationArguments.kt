package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import net.primal.android.explore.asearch.AdvancedSearchContract
import net.primal.android.explore.search.ui.SearchScope
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.nostr.ReactionType
import net.primal.domain.wallet.DraftTx

const val NOTE_ID = "noteId"
inline val SavedStateHandle.noteIdOrThrow: String
    get() = get(NOTE_ID) ?: throw IllegalArgumentException("Missing required noteId argument.")

const val PRIMAL_NAME = "primalName"
inline val SavedStateHandle.primalName: String? get() = get(PRIMAL_NAME)

const val PROMO_CODE = "promoCode"
inline val SavedStateHandle.promoCode: String? get() = get(PROMO_CODE)

const val PROFILE_NPUB = "profileNpub"
inline val SavedStateHandle.npub: String? get() = get(PROFILE_NPUB)

const val IDENTIFIER = "identifier"
inline val SavedStateHandle.identifier: String? get() = get(IDENTIFIER)

const val PROFILE_ID = "profileId"
inline val SavedStateHandle.profileId: String? get() = get(PROFILE_ID)
inline val SavedStateHandle.profileIdOrThrow: String
    get() = get(PROFILE_ID) ?: throw IllegalArgumentException("Missing required profileId argument")

const val EVENT_ID = "eventId"
inline val SavedStateHandle.eventId: String? get() = get(EVENT_ID)
inline val SavedStateHandle.eventIdOrThrow: String
    get() = eventId ?: throw IllegalArgumentException("Missing required eventId argument")

const val ARTICLE_A_TAG = "articleATag"
inline val SavedStateHandle.articleATag: String? get() = get(ARTICLE_A_TAG)

const val ARTICLE_ID = "articleId"
inline val SavedStateHandle.articleId: String? get() = get(ARTICLE_ID)

const val STREAM_NADDR = "streamNaddr"
inline val SavedStateHandle.streamNaddr: String? get() = get(STREAM_NADDR)

const val ARTICLE_NADDR = "articleNaddr"
inline val SavedStateHandle.articleNaddr: String? get() = get(ARTICLE_NADDR)
inline val SavedStateHandle.articleNaddrOrThrow: String
    get() = get(ARTICLE_NADDR) ?: throw IllegalArgumentException("Missing required articleNaddr argument.")

const val FOLLOWS_TYPE = "followsType"
inline val SavedStateHandle.followsType: String? get() = get(FOLLOWS_TYPE)

const val RENDER_TYPE = "renderType"
inline val SavedStateHandle.renderType: String
    get() = get<String>(RENDER_TYPE) ?: throw IllegalArgumentException("Missing required renderType argument.")

const val EXTEND_EXISTING_PREMIUM_NAME = "extendExistingPremiumName"
inline val SavedStateHandle.extendExistingPremiumName: String? get() = get(EXTEND_EXISTING_PREMIUM_NAME)

const val UPGRADE_TO_PRIMAL_PRO = "upgradeToPrimalPro"
inline val SavedStateHandle.upgradeToPrimalPro: Boolean get() = get<String?>(UPGRADE_TO_PRIMAL_PRO) == "true"

const val FROM_ORIGIN_PREMIUM_BADGE = "OriginPremiumBadge"
const val FROM_ORIGIN = "buyingPremiumFromOrigin"
inline val SavedStateHandle.buyingPremiumFromOrigin: String? get() = get(FROM_ORIGIN)
inline val NavBackStackEntry.buyingPremiumFromOrigin: String? get() = arguments?.getString(FROM_ORIGIN)

const val INITIAL_QUERY = "initialQuery"
inline val SavedStateHandle.initialQuery: String? get() = get(INITIAL_QUERY)

const val POSTED_BY = "postedBy"
inline val SavedStateHandle.postedBy: List<String>?
    get() = get<String>(POSTED_BY)?.decodeFromJsonStringOrNull()

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

const val ADVANCED_SEARCH_FEED_SPEC = "advancedSearchFeedSpec"
inline val SavedStateHandle.advancedSearchFeedSpec: String?
    get() = get<String>(ADVANCED_SEARCH_FEED_SPEC)?.ifEmpty { null }

const val FOLLOW_PACK_ID = "followPackId"
inline val SavedStateHandle.followPackIdOrThrow: String
    get() = get(FOLLOW_PACK_ID) ?: throw IllegalArgumentException("Missing required $FOLLOW_PACK_ID argument.")

const val EXPLORE_FEED_SPEC = "exploreFeedSpec"
inline val SavedStateHandle.exploreFeedSpec: String?
    get() = get<String>(EXPLORE_FEED_SPEC)?.asBase64Decoded()?.ifEmpty { null }

const val EXPLORE_FEED_TITLE = "exploreFeedTitle"
inline val SavedStateHandle.exploreFeedTitle: String?
    get() = get<String>(EXPLORE_FEED_TITLE)?.asBase64Decoded()?.ifEmpty { null }

const val EXPLORE_FEED_DESCRIPTION = "exploreFeedDescription"
inline val SavedStateHandle.exploreFeedDescription: String?
    get() = get<String>(EXPLORE_FEED_DESCRIPTION)?.asBase64Decoded()?.ifEmpty { null }

const val NOTE_EDITOR_ARGS = "preFillContent"

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
        ?.asBase64Decoded()?.decodeFromJsonStringOrNull()
        ?: throw IllegalArgumentException("Missing draft transaction.")

const val LNBC = "lnbc"
inline val SavedStateHandle.lnbc: String? get() = get<String>(LNBC)

const val TRANSACTION_ID = "transactionId"
inline val SavedStateHandle.transactionIdOrThrow: String
    get() = get(TRANSACTION_ID) ?: throw IllegalArgumentException("Missing required transactionId argument.")

const val INITIAL_REACTION_TYPE = "initialReactionType"
inline val SavedStateHandle.reactionTypeOrThrow: ReactionType
    get() = get<String>(INITIAL_REACTION_TYPE)
        ?.let { ReactionType.valueOf(it) }
        ?: throw IllegalArgumentException("Missing required $INITIAL_REACTION_TYPE argument.")

const val NOSTR_CONNECT_NAME = "nostrConnectName"
const val NOSTR_CONNECT_URL = "nostrConnectUrl"
const val NOSTR_CONNECT_IMAGE_URL = "nostrConnectImageUrl"
