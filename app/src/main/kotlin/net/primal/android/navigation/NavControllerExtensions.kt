package net.primal.android.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.explore.asearch.AdvancedSearchContract
import net.primal.android.explore.feed.ExploreFeedContract
import net.primal.android.explore.search.ui.SearchScope
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.ReactionType

internal fun NavController.navigateToWelcome() =
    navigate(
        route = "welcome",
        navOptions = navOptions { clearBackStack() },
    )

internal fun NavController.navigateToLogin() = navigate(route = "login")

internal fun NavController.navigateToOnboarding(promoCode: String? = null) =
    navigate(route = "onboarding?$PROMO_CODE=$promoCode")

internal fun NavController.navigateToWalletOnboarding(promoCode: String?) =
    navigate(route = "onboardingWallet?$PROMO_CODE=$promoCode")

internal fun NavController.navigateToLogout(profileId: String) = navigate(route = "logout?$PROFILE_ID=$profileId")

internal fun NavController.navigateToSearch(searchScope: SearchScope) =
    navigate(route = "search?$SEARCH_SCOPE=$searchScope")

internal fun NavController.navigateToAdvancedSearch(
    initialQuery: String? = null,
    initialPostedBy: List<String>? = null,
    initialSearchKind: AdvancedSearchContract.SearchKind? = null,
    initialSearchScope: AdvancedSearchContract.SearchScope? = null,
) = navigate(
    route = "asearch" +
        "?$INITIAL_QUERY=$initialQuery" +
        "&$POSTED_BY=${initialPostedBy.encodeToJsonString()}" +
        "&$SEARCH_KIND=$initialSearchKind" +
        "&$ADV_SEARCH_SCOPE=$initialSearchScope",
)

internal fun NavController.navigateToNoteEditor(args: NoteEditorArgs? = null) {
    navigate(route = "noteEditor?$NOTE_EDITOR_ARGS=${args?.toJson()?.asBase64Encoded()}")
}

internal val NavController.topLevelNavOptions: NavOptions
    @SuppressWarnings("RestrictedApi")
    get() {
        val feedDestination = currentBackStack.value.find {
            it.destination.route?.contains("home") == true
        }
        return navOptions {
            popUpTo(id = feedDestination?.destination?.id ?: 0)
        }
    }

internal fun NavController.navigateToHome() =
    navigate(
        route = "home",
        navOptions = navOptions { clearBackStack() },
    )

internal fun NavController.navigateToReads() =
    navigate(
        route = "reads",
        navOptions = topLevelNavOptions,
    )

internal fun NavController.navigateToWallet() =
    navigate(
        route = "wallet",
        navOptions = topLevelNavOptions,
    )

internal fun NavController.navigateToNotifications() =
    navigate(
        route = "notifications",
        navOptions = topLevelNavOptions,
    )

internal fun NavController.navigateToExplore() =
    navigate(
        route = "explore",
        navOptions = topLevelNavOptions,
    )

internal fun NavController.navigateToFollowPack(profileId: String, followPackId: String) =
    navigate(route = "explore/followPack/$profileId/$followPackId")

internal fun NavController.navigateToRedeemCode(promoCode: String? = null) =
    navigate(route = "redeemCode?$PROMO_CODE=$promoCode")

internal fun NavController.navigateToMessages() = navigate(route = "messages")

internal fun NavController.navigateToChat(profileId: String) = navigate(route = "messages/$profileId")

internal fun NavController.navigateToNewMessage() = navigate(route = "messages/new")

internal fun NavController.navigateToProfile(profileId: String) = navigate(route = "profile?$PROFILE_ID=$profileId")

internal fun NavController.navigateToProfileQrCodeViewer(profileId: String? = null) =
    when {
        profileId != null -> navigate(route = "profileQrCodeViewer?$PROFILE_ID=$profileId")
        else -> navigate(route = "profileQrCodeViewer")
    }

internal fun NavController.navigateToProfileFollows(profileId: String, followsType: ProfileFollowsType) =
    navigate(route = "profile/$profileId/follows?$FOLLOWS_TYPE=$followsType")

internal fun NavController.navigateToProfileEditor() = navigate(route = "profileEditor")

internal fun NavController.navigateToSettings() = navigate(route = "settings")

internal fun NavController.navigateToThread(noteId: String) = navigate(route = "thread/$noteId")

internal fun NavController.navigateToArticleDetails(naddr: String) = navigate(route = "article?$NADDR=$naddr")

internal fun NavController.navigateToReactions(
    eventId: String,
    initialTab: ReactionType = ReactionType.ZAPS,
    articleATag: String?,
) = navigate("reactions/$eventId?$INITIAL_REACTION_TYPE=${initialTab.name}&$ARTICLE_A_TAG=$articleATag")

internal fun NavController.navigateToMediaGallery(
    noteId: String,
    mediaUrl: String,
    mediaPositionMs: Long = 0,
) = navigate(
    route = "media/$noteId" +
        "?$MEDIA_URL=$mediaUrl" +
        "&$MEDIA_POSITION_MS=$mediaPositionMs",
)

internal fun NavController.navigateToMediaItem(mediaUrl: String) {
    val encodedUrl = mediaUrl.asUrlEncoded()
    navigate(route = "mediaItem?$MEDIA_URL=$encodedUrl")
}

internal fun NavController.navigateToExploreFeed(
    feedSpec: String,
    renderType: ExploreFeedContract.RenderType = ExploreFeedContract.RenderType.List,
    feedTitle: String? = null,
    feedDescription: String? = null,
) = navigate(
    route = "explore/note?$EXPLORE_FEED_SPEC=${feedSpec.asBase64Encoded()}" +
        "&$EXPLORE_FEED_TITLE=${feedTitle?.asBase64Encoded()}" +
        "&$EXPLORE_FEED_DESCRIPTION=${feedDescription?.asBase64Encoded()}" +
        "&$RENDER_TYPE=$renderType",
)

internal fun NavController.navigateToBookmarks() = navigate(route = "bookmarks")

internal fun NavController.navigateToPremiumBuying(fromOrigin: String? = null) {
    if (fromOrigin?.isNotEmpty() == true) {
        navigate(route = "premium/buying?$FROM_ORIGIN=$fromOrigin")
    } else {
        navigate(route = "premium/buying")
    }
}

internal fun NavController.navigateToUpgradeToPrimalPro() =
    navigate(route = "premium/buying?$UPGRADE_TO_PRIMAL_PRO=true")

internal fun NavController.navigateToPremiumExtendSubscription(primalName: String) =
    navigate(route = "premium/buying?$EXTEND_EXISTING_PREMIUM_NAME=$primalName")

internal fun NavController.navigateToPremiumHome() = navigate(route = "premium/home")
internal fun NavController.navigateToPremiumSupportPrimal() = navigate(route = "premium/supportPrimal")
internal fun NavController.navigateToLegendContributePrimal() = navigate(route = "premium/legend/contribution")
internal fun NavController.navigateToPremiumMoreInfo(tabIndex: Int = 0) =
    navigate(route = "premium/info?$PREMIUM_MORE_INFO_TAB_INDEX=$tabIndex")

internal fun NavController.navigateToPremiumBuyPrimalLegend(fromOrigin: String? = null) {
    if (fromOrigin?.isNotEmpty() == true) {
        navigate(route = "premium/legend/buy?$FROM_ORIGIN=$fromOrigin")
    } else {
        navigate(route = "premium/legend/buy")
    }
}

internal fun NavController.navigateToPremiumLegendaryProfile() = navigate(route = "premium/legend/profile")
internal fun NavController.navigateToPremiumCard(profileId: String) = navigate(route = "premium/card/$profileId")
internal fun NavController.navigateToPremiumLegendLeaderboard() = navigate(route = "premium/legend/leaderboard")
internal fun NavController.navigateToPremiumOGsLeaderboard() = navigate(route = "premium/ogs/leaderboard")

internal fun NavController.navigateToPremiumManage() = navigate(route = "premium/manage")
internal fun NavController.navigateToPremiumMediaManagement() = navigate(route = "premium/manage/media")
internal fun NavController.navigateToPremiumContactList() = navigate(route = "premium/manage/contacts")
internal fun NavController.navigateToPremiumContentBackup() = navigate(route = "premium/manage/content")
internal fun NavController.navigateToPremiumChangePrimalName() = navigate(route = "premium/manage/changePrimalName")
internal fun NavController.navigateToPremiumOrderHistory() = navigate(route = "premium/manage/order")
internal fun NavController.navigateToPremiumRelay() = navigate(route = "premium/manage/relay")
