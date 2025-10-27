package net.primal.android.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import net.primal.android.articles.reads.ReadsScreen
import net.primal.android.articles.reads.ReadsScreenContract
import net.primal.android.articles.reads.ReadsViewModel
import net.primal.android.auth.login.LoginContract
import net.primal.android.auth.login.LoginScreen
import net.primal.android.auth.login.LoginViewModel
import net.primal.android.auth.logout.LogoutContract
import net.primal.android.auth.logout.LogoutScreen
import net.primal.android.auth.logout.LogoutViewModel
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingViewModel
import net.primal.android.auth.onboarding.account.ui.OnboardingScreen
import net.primal.android.auth.onboarding.wallet.OnboardingWalletActivation
import net.primal.android.auth.welcome.WelcomeContract
import net.primal.android.auth.welcome.WelcomeScreen
import net.primal.android.bookmarks.list.BookmarksContract
import net.primal.android.bookmarks.list.BookmarksScreen
import net.primal.android.bookmarks.list.BookmarksViewModel
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.UnlockScreenOrientation
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorOverlay
import net.primal.android.core.pip.PiPManagerProvider
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.editor.NoteEditorContract
import net.primal.android.editor.NoteEditorScreen
import net.primal.android.editor.di.noteEditorViewModel
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.editor.domain.NoteEditorArgs.Companion.jsonAsNoteEditorArgs
import net.primal.android.events.gallery.EventMediaGalleryScreen
import net.primal.android.events.gallery.EventMediaGalleryViewModel
import net.primal.android.events.reactions.ReactionsContract
import net.primal.android.events.reactions.ReactionsViewModel
import net.primal.android.events.reactions.ui.ReactionsScreen
import net.primal.android.explore.asearch.AdvancedSearchContract
import net.primal.android.explore.asearch.AdvancedSearchScreen
import net.primal.android.explore.asearch.AdvancedSearchViewModel
import net.primal.android.explore.feed.ExploreFeedContract
import net.primal.android.explore.feed.ExploreFeedScreen
import net.primal.android.explore.feed.ExploreFeedViewModel
import net.primal.android.explore.home.ExploreHomeContract
import net.primal.android.explore.home.ExploreHomeScreen
import net.primal.android.explore.home.ExploreHomeViewModel
import net.primal.android.explore.home.followpack.FollowPackContract
import net.primal.android.explore.home.followpack.FollowPackScreen
import net.primal.android.explore.home.followpack.FollowPackViewModel
import net.primal.android.explore.search.SearchContract
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.explore.search.ui.SearchScope
import net.primal.android.explore.search.ui.SearchScreen
import net.primal.android.media.MediaItemScreen
import net.primal.android.media.MediaItemViewModel
import net.primal.android.messages.chat.ChatScreen
import net.primal.android.messages.chat.ChatViewModel
import net.primal.android.messages.conversation.MessageConversationListContract
import net.primal.android.messages.conversation.MessageConversationListViewModel
import net.primal.android.messages.conversation.MessageListScreen
import net.primal.android.messages.conversation.create.NewConversationContract
import net.primal.android.messages.conversation.create.NewConversationScreen
import net.primal.android.nostrconnect.NostrConnectBottomSheet
import net.primal.android.nostrconnect.NostrConnectViewModel
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.home.HomeFeedContract
import net.primal.android.notes.home.HomeFeedScreen
import net.primal.android.notes.home.HomeFeedViewModel
import net.primal.android.notifications.list.NotificationsContract
import net.primal.android.notifications.list.NotificationsScreen
import net.primal.android.notifications.list.NotificationsViewModel
import net.primal.android.premium.buying.PremiumBuyingContract
import net.primal.android.premium.buying.PremiumBuyingScreen
import net.primal.android.premium.buying.PremiumBuyingViewModel
import net.primal.android.premium.card.PremiumCardContract
import net.primal.android.premium.card.PremiumCardScreen
import net.primal.android.premium.card.PremiumCardViewModel
import net.primal.android.premium.home.PremiumHomeContract
import net.primal.android.premium.home.PremiumHomeScreen
import net.primal.android.premium.home.PremiumHomeViewModel
import net.primal.android.premium.info.MORE_INFO_FAQ_TAB_INDEX
import net.primal.android.premium.info.PremiumMoreInfoContract
import net.primal.android.premium.info.PremiumMoreInfoScreen
import net.primal.android.premium.leaderboard.legend.LegendLeaderboardContract
import net.primal.android.premium.leaderboard.legend.LegendLeaderboardScreen
import net.primal.android.premium.leaderboard.legend.LegendLeaderboardViewModel
import net.primal.android.premium.leaderboard.ogs.OGLeaderboardContract
import net.primal.android.premium.leaderboard.ogs.OGLeaderboardScreen
import net.primal.android.premium.leaderboard.ogs.OGLeaderboardViewModel
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract
import net.primal.android.premium.legend.become.PremiumBecomeLegendScreen
import net.primal.android.premium.legend.become.PremiumBecomeLegendViewModel
import net.primal.android.premium.legend.contribute.LegendContributeContract
import net.primal.android.premium.legend.contribute.LegendContributeScreen
import net.primal.android.premium.legend.contribute.LegendContributeViewModel
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationContract
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationScreen
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationViewModel
import net.primal.android.premium.manage.PremiumManageContract
import net.primal.android.premium.manage.PremiumManageScreen
import net.primal.android.premium.manage.PremiumManageViewModel
import net.primal.android.premium.manage.contact.PremiumContactListScreen
import net.primal.android.premium.manage.contact.PremiumContactListViewModel
import net.primal.android.premium.manage.content.PremiumContentBackupScreen
import net.primal.android.premium.manage.content.PremiumContentBackupViewModel
import net.primal.android.premium.manage.media.PremiumMediaManagementScreen
import net.primal.android.premium.manage.media.PremiumMediaManagementViewModel
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameScreen
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameViewModel
import net.primal.android.premium.manage.order.PremiumOrderHistoryContract
import net.primal.android.premium.manage.order.PremiumOrderHistoryScreen
import net.primal.android.premium.manage.order.PremiumOrderHistoryViewModel
import net.primal.android.premium.manage.relay.PremiumRelayScreen
import net.primal.android.premium.manage.relay.PremiumRelayViewModel
import net.primal.android.premium.support.SupportPrimalContract
import net.primal.android.premium.support.SupportPrimalScreen
import net.primal.android.premium.support.SupportPrimalViewModel
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.details.ProfileDetailsScreen
import net.primal.android.profile.details.ProfileDetailsViewModel
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.profile.editor.ProfileEditorContract
import net.primal.android.profile.editor.ProfileEditorViewModel
import net.primal.android.profile.editor.ui.ProfileEditorScreen
import net.primal.android.profile.follows.ProfileFollowsContract
import net.primal.android.profile.follows.ProfileFollowsScreen
import net.primal.android.profile.follows.ProfileFollowsViewModel
import net.primal.android.profile.qr.ProfileQrCodeContract
import net.primal.android.profile.qr.ProfileQrCodeViewModel
import net.primal.android.profile.qr.ui.ProfileQrCodeViewerScreen
import net.primal.android.redeem.RedeemCodeContract
import net.primal.android.redeem.RedeemCodeScreen
import net.primal.android.redeem.RedeemCodeViewModel
import net.primal.android.stream.LiveStreamOverlay
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.thread.articles.details.ArticleDetailsContract
import net.primal.android.thread.articles.details.ArticleDetailsScreen
import net.primal.android.thread.articles.details.ArticleDetailsViewModel
import net.primal.android.thread.notes.ThreadContract
import net.primal.android.thread.notes.ThreadScreen
import net.primal.android.thread.notes.ThreadViewModel
import net.primal.android.wallet.activation.WalletActivationContract
import net.primal.android.wallet.activation.WalletActivationViewModel
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.feeds.buildAdvancedSearchNotesFeedSpec
import net.primal.domain.feeds.buildAdvancedSearchNotificationsFeedSpec
import net.primal.domain.feeds.buildAdvancedSearchReadsFeedSpec
import net.primal.domain.feeds.buildReadsTopicFeedSpec
import net.primal.domain.nostr.ReactionType

private fun NavController.navigateToWelcome() =
    navigate(
        route = "welcome",
        navOptions = navOptions { clearBackStack() },
    )

private fun NavController.navigateToLogin() = navigate(route = "login")

private fun NavController.navigateToOnboarding(promoCode: String? = null) =
    navigate(route = "onboarding?$PROMO_CODE=$promoCode")

private fun NavController.navigateToWalletOnboarding(promoCode: String?) =
    navigate(route = "onboardingWallet?$PROMO_CODE=$promoCode")

private fun NavController.navigateToLogout(profileId: String) = navigate(route = "logout?$PROFILE_ID=$profileId")

private fun NavController.navigateToSearch(searchScope: SearchScope) =
    navigate(route = "search?$SEARCH_SCOPE=$searchScope")

private fun NavController.navigateToAdvancedSearch(
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

private fun NavController.navigateToNoteEditor(args: NoteEditorArgs? = null) {
    navigate(route = "noteEditor?$NOTE_EDITOR_ARGS=${args?.toJson()?.asBase64Encoded()}")
}

private val NavController.topLevelNavOptions: NavOptions
    @SuppressWarnings("RestrictedApi")
    get() {
        val feedDestination = currentBackStack.value.find {
            it.destination.route?.contains("home") == true
        }
        return navOptions {
            popUpTo(id = feedDestination?.destination?.id ?: 0)
        }
    }

fun NavController.navigateToHome() =
    navigate(
        route = "home",
        navOptions = navOptions { clearBackStack() },
    )

fun NavController.navigateToReads() =
    navigate(
        route = "reads",
        navOptions = topLevelNavOptions,
    )

fun NavController.navigateToWallet() =
    navigate(
        route = "wallet",
        navOptions = topLevelNavOptions,
    )

private fun NavController.navigateToNotifications() =
    navigate(
        route = "notifications",
        navOptions = topLevelNavOptions,
    )

fun NavController.navigateToExplore() =
    navigate(
        route = "explore",
        navOptions = topLevelNavOptions,
    )

fun NavController.navigateToFollowPack(profileId: String, followPackId: String) =
    navigate(route = "explore/followPack/$profileId/$followPackId")

fun NavController.navigateToScanCode(promoCode: String? = null) = navigate(route = "scanCode?$PROMO_CODE=$promoCode")

private fun NavController.navigateToMessages() = navigate(route = "messages")

fun NavController.navigateToChat(profileId: String) = navigate(route = "messages/$profileId")

private fun NavController.navigateToNewMessage() = navigate(route = "messages/new")

fun NavController.navigateToProfile(profileId: String) = navigate(route = "profile?$PROFILE_ID=$profileId")

fun NavController.navigateToProfileQrCodeViewer(profileId: String? = null) =
    when {
        profileId != null -> navigate(route = "profileQrCodeViewer?$PROFILE_ID=$profileId")
        else -> navigate(route = "profileQrCodeViewer")
    }

fun NavController.navigateToProfileFollows(profileId: String, followsType: ProfileFollowsType) =
    navigate(route = "profile/$profileId/follows?$FOLLOWS_TYPE=$followsType")

fun NavController.navigateToProfileEditor() = navigate(route = "profileEditor")

private fun NavController.navigateToSettings() = navigate(route = "settings")

fun NavController.navigateToThread(noteId: String) = navigate(route = "thread/$noteId")

fun NavController.navigateToArticleDetails(naddr: String) = navigate(route = "article?$ARTICLE_NADDR=$naddr")

fun NavController.navigateToReactions(
    eventId: String,
    initialTab: ReactionType = ReactionType.ZAPS,
    articleATag: String?,
) = navigate("reactions/$eventId?$INITIAL_REACTION_TYPE=${initialTab.name}&$ARTICLE_A_TAG=$articleATag")

fun NavController.navigateToMediaGallery(
    noteId: String,
    mediaUrl: String,
    mediaPositionMs: Long = 0,
) = navigate(
    route = "media/$noteId" +
        "?$MEDIA_URL=$mediaUrl" +
        "&$MEDIA_POSITION_MS=$mediaPositionMs",
)

fun NavController.navigateToMediaItem(mediaUrl: String) {
    val encodedUrl = mediaUrl.asUrlEncoded()
    navigate(route = "mediaItem?$MEDIA_URL=$encodedUrl")
}

fun NavController.navigateToExploreFeed(
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

private fun NavController.navigateToBookmarks() = navigate(route = "bookmarks")

fun NavController.navigateToPremiumBuying(fromOrigin: String? = null) {
    if (fromOrigin?.isNotEmpty() == true) {
        navigate(route = "premium/buying?$FROM_ORIGIN=$fromOrigin")
    } else {
        navigate(route = "premium/buying")
    }
}

private fun NavController.navigateToUpgradeToPrimalPro() =
    navigate(route = "premium/buying?$UPGRADE_TO_PRIMAL_PRO=true")

private fun NavController.navigateToPremiumExtendSubscription(primalName: String) =
    navigate(route = "premium/buying?$EXTEND_EXISTING_PREMIUM_NAME=$primalName")

private fun NavController.navigateToPremiumHome() = navigate(route = "premium/home")
private fun NavController.navigateToPremiumSupportPrimal() = navigate(route = "premium/supportPrimal")
private fun NavController.navigateToLegendContributePrimal() = navigate(route = "premium/legend/contribution")
private fun NavController.navigateToPremiumMoreInfo(tabIndex: Int = 0) =
    navigate(route = "premium/info?$PREMIUM_MORE_INFO_TAB_INDEX=$tabIndex")

private fun NavController.navigateToPremiumBuyPrimalLegend(fromOrigin: String? = null) {
    if (fromOrigin?.isNotEmpty() == true) {
        navigate(route = "premium/legend/buy?$FROM_ORIGIN=$fromOrigin")
    } else {
        navigate(route = "premium/legend/buy")
    }
}

private fun NavController.navigateToPremiumLegendaryProfile() = navigate(route = "premium/legend/profile")
private fun NavController.navigateToPremiumCard(profileId: String) = navigate(route = "premium/card/$profileId")
private fun NavController.navigateToPremiumLegendLeaderboard() = navigate(route = "premium/legend/leaderboard")
private fun NavController.navigateToPremiumOGsLeaderboard() = navigate(route = "premium/ogs/leaderboard")

private fun NavController.navigateToPremiumManage() = navigate(route = "premium/manage")
private fun NavController.navigateToPremiumMediaManagement() = navigate(route = "premium/manage/media")
private fun NavController.navigateToPremiumContactList() = navigate(route = "premium/manage/contacts")
private fun NavController.navigateToPremiumContentBackup() = navigate(route = "premium/manage/content")
private fun NavController.navigateToPremiumChangePrimalName() = navigate(route = "premium/manage/changePrimalName")
private fun NavController.navigateToPremiumOrderHistory() = navigate(route = "premium/manage/order")
private fun NavController.navigateToPremiumRelay() = navigate(route = "premium/manage/relay")

private fun NavController.navigateToNostrConnectBottomSheet(url: String) {
    val safeUrl = url.asUrlEncoded()
    navigate(route = "nostrConnectBottomSheet?$NOSTR_CONNECT_URI=$safeUrl")
}

fun accountSwitcherCallbacksHandler(navController: NavController) =
    AccountSwitcherCallbacks(
        onActiveAccountChanged = { navController.navigateToHome() },
        onAddExistingAccountClick = { navController.navigateToLogin() },
        onCreateNewAccountClick = { navController.navigateToOnboarding() },
    )

fun noteCallbacksHandler(navController: NavController) =
    NoteCallbacks(
        onNoteClick = { noteId -> navController.navigateToThread(noteId = noteId) },
        onNoteReplyClick = { referencedNoteEvent ->
            navController.navigateToNoteEditor(NoteEditorArgs(referencedNoteNevent = referencedNoteEvent))
        },
        onNoteQuoteClick = { noteNevent ->
            navController.navigateToNoteEditor(
                args = NoteEditorArgs(
                    referencedNoteNevent = noteNevent,
                    isQuoting = true,
                ),
            )
        },
        onStreamQuoteClick = { streamNaddr ->
            navController.navigateToNoteEditor(
                args = NoteEditorArgs(
                    referencedStreamNaddr = streamNaddr,
                    isQuoting = true,
                ),
            )
        },
        onHighlightReplyClick = { highlightNevent, articleNaddr ->
            navController.navigateToNoteEditor(
                args = NoteEditorArgs(
                    referencedHighlightNevent = highlightNevent,
                    referencedArticleNaddr = articleNaddr,
                ),
            )
        },
        onHighlightQuoteClick = { nevent, naddr ->
            navController.navigateToNoteEditor(
                args = NoteEditorArgs(
                    referencedArticleNaddr = naddr,
                    referencedHighlightNevent = nevent,
                    isQuoting = true,
                ),
            )
        },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onArticleReplyClick = { naddr ->
            navController.navigateToNoteEditor(
                NoteEditorArgs(referencedArticleNaddr = naddr),
            )
        },
        onArticleQuoteClick = { naddr ->
            navController.navigateToNoteEditor(
                args = NoteEditorArgs(
                    referencedArticleNaddr = naddr,
                    isQuoting = true,
                ),
            )
        },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onHashtagClick = { hashtag ->
            navController.navigateToExploreFeed(feedSpec = buildAdvancedSearchNotesFeedSpec(query = hashtag))
        },
        onMediaClick = {
            navController.navigateToMediaGallery(
                noteId = it.noteId,
                mediaUrl = it.mediaUrl,
                mediaPositionMs = it.positionMs,
            )
        },
        onPayInvoiceClick = {
            navController.navigateToWalletCreateTransaction(lnbc = it.lnbc)
        },
        onEventReactionsClick = { eventId, initialTab, articleATag ->
            navController.navigateToReactions(eventId = eventId, initialTab = initialTab, articleATag = articleATag)
        },
        onGetPrimalPremiumClick = { navController.navigateToPremiumBuying() },
        onPrimalLegendsLeaderboardClick = { navController.navigateToPremiumLegendLeaderboard() },
    )

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PrimalAppNavigation(startDestination: String) {
    val navController = rememberNavController()

    val topLevelDestinationHandler: (PrimalTopLevelDestination) -> Unit = {
        when (it) {
            PrimalTopLevelDestination.Home -> navController.popBackStack()
            PrimalTopLevelDestination.Reads -> navController.navigateToReads()
            PrimalTopLevelDestination.Wallet -> navController.navigateToWallet()
            PrimalTopLevelDestination.Notifications -> navController.navigateToNotifications()
            PrimalTopLevelDestination.Explore -> navController.navigateToExplore()
        }
    }

    val drawerDestinationHandler: (DrawerScreenDestination) -> Unit = {
        when (it) {
            is DrawerScreenDestination.Profile -> navController.navigateToProfile(profileId = it.userId)
            is DrawerScreenDestination.Premium -> if (it.hasPremium) {
                navController.navigateToPremiumHome()
            } else {
                navController.navigateToPremiumBuying()
            }

            DrawerScreenDestination.Messages -> navController.navigateToMessages()
            is DrawerScreenDestination.Bookmarks -> navController.navigateToBookmarks()
            DrawerScreenDestination.ScanCode -> navController.navigateToScanCode()
            DrawerScreenDestination.Settings -> navController.navigateToSettings()
            is DrawerScreenDestination.SignOut -> navController.navigateToLogout(profileId = it.userId)
        }
    }

    SharedTransitionLayout {
        ConnectionIndicatorOverlay {
            PiPManagerProvider {
                LiveStreamOverlay(
                    navController = navController,
                    noteCallbacks = noteCallbacksHandler(navController = navController),
                ) {
                    PrimalAppNavigation(
                        navController = navController,
                        startDestination = startDestination,
                        drawerDestinationHandler = drawerDestinationHandler,
                        topLevelDestinationHandler = topLevelDestinationHandler,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PrimalAppNavigation(
    navController: NavHostController,
    startDestination: String,
    drawerDestinationHandler: (DrawerScreenDestination) -> Unit,
    topLevelDestinationHandler: (PrimalTopLevelDestination) -> Unit,
) {
    NavHost(
        modifier = Modifier.background(AppTheme.colorScheme.background),
        navController = navController,
        startDestination = startDestination,
    ) {
        welcome(route = "welcome", navController = navController)

        login(route = "login", navController = navController)

        onboarding(
            route = "onboarding?$PROMO_CODE={$PROMO_CODE}",
            arguments = listOf(
                navArgument(PROMO_CODE) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )

        onboardingWalletActivation(
            route = "onboardingWallet?$PROMO_CODE={$PROMO_CODE}",
            arguments = listOf(
                navArgument(PROMO_CODE) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )

        scanCode(
            route = "scanCode?$PROMO_CODE={$PROMO_CODE}",
            arguments = listOf(
                navArgument(PROMO_CODE) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/rc/{$PROMO_CODE}"
                },
            ),
            navController = navController,
        )

        nostrConnectDialog(
            route = "nostrConnectBottomSheet?$NOSTR_CONNECT_URI={$NOSTR_CONNECT_URI}",
            arguments = listOf(
                navArgument(NOSTR_CONNECT_URI) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )

        logout(
            route = "logout?$PROFILE_ID={$PROFILE_ID}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                    nullable = false
                },
            ),
            navController = navController,
        )

        home(
            route = "home",
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
            arguments = listOf(
                navArgument(PROFILE_NPUB) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(IDENTIFIER) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(PRIMAL_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(STREAM_NADDR) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net"
                },
                navDeepLink {
                    uriPattern = "https://primal.net/home"
                },
                navDeepLink {
                    uriPattern = "https://primal.net/p/{$PROFILE_NPUB}/live/{$IDENTIFIER}"
                },
                navDeepLink {
                    uriPattern = "https://primal.net/{$PRIMAL_NAME}/live/{$IDENTIFIER}"
                },
                navDeepLink {
                    uriPattern = "primal://live/{$STREAM_NADDR}"
                },
            ),
        )

        reads(
            route = "reads",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/reads"
                },
            ),
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
        )

        explore(
            route = "explore",
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/explore"
                },
            ),
        )

        followPack(
            route = "explore/followPack/{$PROFILE_ID}/{$FOLLOW_PACK_ID}",
            navController = navController,
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(FOLLOW_PACK_ID) {
                    type = NavType.StringType
                    nullable = false
                },
            ),
        )

        bookmarks(
            route = "bookmarks",
            navController = navController,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/bookmarks"
                },
            ),
        )

        exploreFeed(
            route = "explore/note?" +
                "$EXPLORE_FEED_SPEC={$EXPLORE_FEED_SPEC}&" +
                "$ADVANCED_SEARCH_FEED_SPEC={$ADVANCED_SEARCH_FEED_SPEC}&" +
                "$EXPLORE_FEED_TITLE={$EXPLORE_FEED_TITLE}&" +
                "$EXPLORE_FEED_DESCRIPTION={$EXPLORE_FEED_DESCRIPTION}&" +
                "$RENDER_TYPE={$RENDER_TYPE}",
            arguments = listOf(
                navArgument(EXPLORE_FEED_SPEC) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(ADVANCED_SEARCH_FEED_SPEC) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(EXPLORE_FEED_TITLE) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(EXPLORE_FEED_DESCRIPTION) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(RENDER_TYPE) {
                    type = NavType.StringType
                    nullable = false
                    defaultValue = ExploreFeedContract.RenderType.List.toString()
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/search/{$ADVANCED_SEARCH_FEED_SPEC}"
                },
            ),
            navController = navController,
        )

        search(
            route = "search?$SEARCH_SCOPE={$SEARCH_SCOPE}",
            arguments = listOf(
                navArgument(SEARCH_SCOPE) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )

        advancedSearch(
            route = "asearch" +
                "?$INITIAL_QUERY={$INITIAL_QUERY}" +
                "&$POSTED_BY={$POSTED_BY}" +
                "&$SEARCH_KIND={$SEARCH_KIND}" +
                "&$ADV_SEARCH_SCOPE={$ADV_SEARCH_SCOPE}",
            navController = navController,
            arguments = listOf(
                navArgument(INITIAL_QUERY) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(POSTED_BY) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(SEARCH_KIND) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(ADV_SEARCH_SCOPE) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        )

        premiumBuying(
            route = "premium/buying" +
                "?$EXTEND_EXISTING_PREMIUM_NAME={$EXTEND_EXISTING_PREMIUM_NAME}" +
                "&$UPGRADE_TO_PRIMAL_PRO={$UPGRADE_TO_PRIMAL_PRO}" +
                "&$FROM_ORIGIN={$FROM_ORIGIN}",
            arguments = listOf(
                navArgument(EXTEND_EXISTING_PREMIUM_NAME) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(UPGRADE_TO_PRIMAL_PRO) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(FROM_ORIGIN) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/premium"
                },
            ),
        )

        premiumHome(
            route = "premium/home",
            navController = navController,
        )

        premiumSupportPrimal(route = "premium/supportPrimal", navController = navController)

        premiumLegendContribution(route = "premium/legend/contribution", navController = navController)

        premiumMoreInfo(
            route = "premium/info?$PREMIUM_MORE_INFO_TAB_INDEX={$PREMIUM_MORE_INFO_TAB_INDEX}",
            arguments = listOf(
                navArgument(PREMIUM_MORE_INFO_TAB_INDEX) {
                    type = NavType.IntType
                    defaultValue = 0
                },
            ),
            navController = navController,
        )

        premiumBuyPrimalLegend(
            route = "premium/legend/buy?$FROM_ORIGIN={$FROM_ORIGIN}",
            arguments = listOf(
                navArgument(FROM_ORIGIN) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )

        premiumLegendaryProfile(route = "premium/legend/profile", navController = navController)

        premiumCard(
            route = "premium/card/{$PROFILE_ID}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )

        premiumLegendLeaderboard(
            route = "premium/legend/leaderboard",
            navController = navController,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/legends"
                },
            ),
        )
        premiumOGsLeaderboard(route = "premium/ogs/leaderboard", navController = navController)

        premiumManage(route = "premium/manage", navController = navController)

        premiumContactList(route = "premium/manage/contacts", navController = navController)

        premiumContentBackup(route = "premium/manage/content", navController = navController)

        premiumMediaManagement(route = "premium/manage/media", navController = navController)

        premiumChangePrimalName(route = "premium/manage/changePrimalName", navController = navController)

        premiumOrderHistory(route = "premium/manage/order", navController = navController)

        premiumRelay(route = "premium/manage/relay", navController = navController)

        messages(
            route = "messages",
            navController = navController,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/dms"
                },
            ),
        )

        chat(
            route = "messages/{$PROFILE_ID}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )

        newMessage(route = "messages/new", navController = navController)

        notifications(
            route = "notifications",
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/notifications"
                },
            ),
        )

        noteEditor(
            route = "noteEditor?$NOTE_EDITOR_ARGS={$NOTE_EDITOR_ARGS}",
            arguments = listOf(
                navArgument(NOTE_EDITOR_ARGS) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    action = "ACTION_SEND"
                    mimeType = "image/*"
                },
                navDeepLink {
                    action = "ACTION_SEND_MULTIPLE"
                    mimeType = "image/*"
                },
                navDeepLink {
                    action = "ACTION_SEND"
                    mimeType = "video/*"
                },
                navDeepLink {
                    action = "ACTION_SEND_MULTIPLE"
                    mimeType = "video/*"
                },
            ),
            navController = navController,
        )

        thread(
            route = "thread/{$NOTE_ID}",
            arguments = listOf(
                navArgument(NOTE_ID) {
                    type = NavType.StringType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/e/{$NOTE_ID}"
                },
            ),
            navController = navController,
        )

        articleDetails(
            route = "article?$ARTICLE_NADDR={$ARTICLE_NADDR}&$PRIMAL_NAME={$PRIMAL_NAME}&$ARTICLE_ID={$ARTICLE_ID}",
            arguments = listOf(
                navArgument(ARTICLE_NADDR) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(PRIMAL_NAME) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(ARTICLE_ID) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/a/{$ARTICLE_NADDR}"
                },
                navDeepLink {
                    uriPattern = "https://primal.net/{$PRIMAL_NAME}/{$ARTICLE_ID}"
                },
            ),
            navController = navController,
        )

        reactions(
            route = "reactions/{$EVENT_ID}" +
                "?$INITIAL_REACTION_TYPE={$INITIAL_REACTION_TYPE}&$ARTICLE_A_TAG={$ARTICLE_A_TAG}",
            arguments = listOf(
                navArgument(EVENT_ID) { type = NavType.StringType },
                navArgument(INITIAL_REACTION_TYPE) {
                    type = NavType.StringType
                    defaultValue = ReactionType.ZAPS.name
                },
                navArgument(ARTICLE_A_TAG) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )

        media(
            route = "media/{$NOTE_ID}" +
                "?$MEDIA_URL={$MEDIA_URL}" +
                "&$MEDIA_POSITION_MS={$MEDIA_POSITION_MS}",
            arguments = listOf(
                navArgument(NOTE_ID) {
                    type = NavType.StringType
                },
                navArgument(MEDIA_URL) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(MEDIA_POSITION_MS) {
                    type = NavType.LongType
                    nullable = false
                    defaultValue = 0
                },
            ),
            navController = navController,
        )

        mediaItem(
            route = "mediaItem?$MEDIA_URL={$MEDIA_URL}",
            arguments = listOf(
                navArgument(MEDIA_URL) {
                    type = NavType.StringType
                    nullable = false
                },
            ),
            navController = navController,
        )

        profile(
            route = "profile?$PROFILE_ID={$PROFILE_ID}&$PRIMAL_NAME={$PRIMAL_NAME}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(PRIMAL_NAME) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://primal.net/p/{$PROFILE_ID}"
                },
                navDeepLink {
                    uriPattern = "https://primal.net/{$PRIMAL_NAME}"
                },
            ),
            navController = navController,
        )

        profileFollows(
            route = "profile/{$PROFILE_ID}/follows?$FOLLOWS_TYPE={$FOLLOWS_TYPE}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                },
                navArgument(FOLLOWS_TYPE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ProfileFollowsType.Following.name
                },
            ),
            navController = navController,
        )

        profileEditor(route = "profileEditor", navController = navController)

        profileQrCodeViewer(
            route = "profileQrCodeViewer?$PROFILE_ID={$PROFILE_ID}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )

        settingsNavigation(route = "settings", navController = navController)

        walletNavigation(
            route = "wallet",
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
        )
    }
}

private fun Intent?.parseMediaUris(): List<String> =
    when (this?.action) {
        Intent.ACTION_SEND -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                this.getParcelableExtra(Intent.EXTRA_STREAM)
            }?.run { listOf(toString()) }
        }

        Intent.ACTION_SEND_MULTIPLE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                this.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }?.map { it.toString() }
        }

        else -> emptyList()
    } ?: emptyList()

private fun NavGraphBuilder.welcome(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = {
            val initialRoute = initialState.destination.route

            when {
                initialRoute == "login" ||
                    initialRoute?.startsWith("onboarding") == true ||
                    initialRoute?.startsWith("redeemCode") == true
                -> slideInHorizontally(initialOffsetX = { -it })

                else -> null
            }
        },
        exitTransition = {
            val targetRoute = targetState.destination.route
            when {
                targetRoute == "login" ||
                    targetRoute?.startsWith("onboarding") == true ||
                    targetRoute?.startsWith("redeemCode") == true
                -> slideOutHorizontally(targetOffsetX = { -it })

                else -> null
            }
        },
    ) {
        LockToOrientationPortrait()
        PrimalTheme(PrimalTheme.Sunset) {
            ApplyEdgeToEdge(isDarkTheme = true)
            WelcomeScreen(
                callbacks = WelcomeContract.ScreenCallbacks(
                    onSignInClick = { navController.navigateToLogin() },
                    onCreateAccountClick = { navController.navigateToOnboarding() },
                    onRedeemCodeClick = { navController.navigateToScanCode() },
                ),
            )
        }
    }

private fun NavGraphBuilder.login(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = {
            when (initialState.destination.route) {
                "welcome" -> slideInHorizontally(initialOffsetX = { it })
                else -> null
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                "welcome" -> slideOutHorizontally(targetOffsetX = { it })
                else -> null
            }
        },
    ) {
        val viewModel: LoginViewModel = hiltViewModel(it)
        LockToOrientationPortrait()
        PrimalTheme(PrimalTheme.Sunset) {
            ApplyEdgeToEdge(isDarkTheme = true)
            LoginScreen(
                viewModel = viewModel,
                callbacks = LoginContract.ScreenCallbacks(
                    onLoginSuccess = { navController.navigateToHome() },
                    onClose = { navController.popBackStack() },
                ),
            )
        }
    }

private fun NavGraphBuilder.onboarding(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = {
        val initialRoute = initialState.destination.route

        when {
            initialRoute == "welcome" ||
                initialRoute?.startsWith("redeemCode") == true ->
                slideInHorizontally(initialOffsetX = { it })

            else -> null
        }
    },
    exitTransition = {
        val targetRoute = targetState.destination.route

        when {
            targetRoute == "welcome" ||
                targetRoute?.startsWith("redeemCode") == true ->
                slideOutHorizontally(targetOffsetX = { it })

            else -> null
        }
    },
) {
    val viewModel: OnboardingViewModel = hiltViewModel(it)
    val promoCode = it.arguments?.getString(PROMO_CODE)

    LockToOrientationPortrait()
    PrimalTheme(PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        OnboardingScreen(
            viewModel = viewModel,
            callbacks = OnboardingContract.ScreenCallbacks(
                onClose = { navController.popBackStack() },
                onOnboarded = { navController.navigateToHome() },
                onActivateWallet = { navController.navigateToWalletOnboarding(promoCode = promoCode) },
            ),
        )
    }
}

private fun NavGraphBuilder.onboardingWalletActivation(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(route = route, arguments = arguments) {
    val viewModel = hiltViewModel<WalletActivationViewModel>()
    PrimalTheme(PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        LockToOrientationPortrait()
        OnboardingWalletActivation(
            viewModel = viewModel,
            callbacks = WalletActivationContract.ScreenCallbacks(
                onDoneOrDismiss = { navController.navigateToHome() },
            ),
        )
    }
}

private fun NavGraphBuilder.scanCode(
    route: String,
    arguments: List<NamedNavArgument>,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        val initialRoute = initialState.destination.route
        when {
            initialRoute == "welcome" || initialRoute.isMainScreenRoute() ->
                slideInHorizontally(initialOffsetX = { it })

            initialRoute?.startsWith("onboarding") == true ->
                slideInHorizontally(initialOffsetX = { -it })

            else -> null
        }
    },
    exitTransition = {
        val targetRoute = targetState.destination.route
        when {
            targetRoute == "welcome" || targetRoute.isMainScreenRoute() ->
                slideOutHorizontally(targetOffsetX = { it })

            targetRoute?.startsWith("onboarding") == true ->
                slideOutHorizontally(targetOffsetX = { -it })

            else -> null
        }
    },
) {
    val viewModel = hiltViewModel<RedeemCodeViewModel>()
    PrimalTheme(PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        LockToOrientationPortrait()
        RedeemCodeScreen(
            viewModel = viewModel,
            callbacks = RedeemCodeContract.ScreenCallbacks(
                onClose = navController::navigateUp,
                navigateToOnboarding = { promoCode -> navController.navigateToOnboarding(promoCode) },
                navigateToWalletOnboarding = { promoCode -> navController.navigateToWalletOnboarding(promoCode) },
                onNostrConnectRequest = { url ->
                    navController.popBackStack()
                    navController.navigateToNostrConnectBottomSheet(url = url)
                },
            ),
        )
    }
}

private fun NavGraphBuilder.home(
    route: String,
    arguments: List<NamedNavArgument>,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
    popEnterTransition = {
        when {
            initialState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleIn
        }
    },
    popExitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
) { navBackEntry ->
    val viewModel = hiltViewModel<HomeFeedViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    HomeFeedScreen(
        viewModel = viewModel,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        noteCallbacks = noteCallbacksHandler(navController),
        accountSwitcherCallbacks = accountSwitcherCallbacksHandler(navController = navController),
        callbacks = HomeFeedContract.ScreenCallbacks(
            onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
            onGoToWallet = { navController.navigateToWallet() },
            onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.Notes) },
            onNewPostClick = { navController.navigateToNoteEditor(null) },
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun NavGraphBuilder.nostrConnectDialog(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) {
    dialog(
        route = route,
        arguments = arguments,
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        val viewModel = hiltViewModel<NostrConnectViewModel>()
        ApplyEdgeToEdge(isDarkTheme = true)
        LockToOrientationPortrait()
        NostrConnectBottomSheet(
            viewModel = viewModel,
            onDismissRequest = { navController.popBackStack() },
        )
    }
}

private fun NavGraphBuilder.reads(
    route: String,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
    popEnterTransition = {
        when {
            initialState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleIn
        }
    },
    popExitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
) { navBackEntry ->
    val viewModel = hiltViewModel<ReadsViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ReadsScreen(
        viewModel = viewModel,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        accountSwitcherCallbacks = accountSwitcherCallbacksHandler(navController = navController),
        callbacks = ReadsScreenContract.ScreenCallbacks(
            onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
            onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.Reads) },
            onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr) },
            onGetPremiumClick = { navController.navigateToPremiumBuying() },
        ),
    )
}

private fun NavGraphBuilder.noteEditor(
    route: String,
    deepLinks: List<NavDeepLink>,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments = arguments,
) {
    val activity = LocalActivity.current
    val mediaUrls = activity?.intent.parseMediaUris()

    val args = it.arguments?.getString(NOTE_EDITOR_ARGS)
        ?.asBase64Decoded()
        ?.jsonAsNoteEditorArgs()
        ?: NoteEditorArgs()
            .copy(mediaUris = mediaUrls)

    val viewModel = noteEditorViewModel(args = args)

    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    NoteEditorScreen(
        viewModel = viewModel,
        callbacks = NoteEditorContract.ScreenCallbacks(
            onClose = {
                activity?.intent?.removeExtra(Intent.EXTRA_STREAM)
                navController.navigateUp()
            },
        ),
    )
}

private fun NavGraphBuilder.explore(
    route: String,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
    popEnterTransition = {
        when {
            initialState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleIn
        }
    },
    popExitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
) {
    val viewModel = hiltViewModel<ExploreHomeViewModel>(it)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ExploreHomeScreen(
        viewModel = viewModel,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        noteCallbacks = noteCallbacksHandler(navController),
        accountSwitcherCallbacks = accountSwitcherCallbacksHandler(navController = navController),
        callbacks = ExploreHomeContract.ScreenCallbacks(
            onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
            onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.Notes) },
            onAdvancedSearchClick = { navController.navigateToAdvancedSearch() },
            onFollowPackClick = { profileId, identifier -> navController.navigateToFollowPack(profileId, identifier) },
            onGoToWallet = { navController.navigateToWallet() },
            onNewPostClick = { navController.navigateToNoteEditor(null) },
        ),
    )
}

private fun NavGraphBuilder.followPack(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<FollowPackViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    FollowPackScreen(
        viewModel = viewModel,
        callbacks = FollowPackContract.ScreenCallbacks(
            onShowFeedClick = { feed, title, description ->
                navController.navigateToExploreFeed(
                    feedSpec = feed,
                    feedTitle = title,
                    feedDescription = description,
                )
            },
            onProfileClick = { navController.navigateToProfile(profileId = it) },
            onClose = { navController.navigateUp() },
        ),
    )
}

private fun NavGraphBuilder.exploreFeed(
    route: String,
    deepLinks: List<NavDeepLink>,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<ExploreFeedViewModel>(it)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ExploreFeedScreen(
        viewModel = viewModel,
        noteCallbacks = noteCallbacksHandler(navController),
        callbacks = ExploreFeedContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onGoToWallet = { navController.navigateToWallet() },
        ),
    )
}

private fun NavGraphBuilder.search(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<SearchViewModel>(it)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    val searchScope = it.searchScopeOrThrow
    SearchScreen(
        viewModel = viewModel,
        searchScope = searchScope,
        callbacks = SearchContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onAdvancedSearchClick = { query ->
                navController.popBackStack()
                when (searchScope) {
                    SearchScope.Notes -> navController.navigateToAdvancedSearch(
                        initialQuery = query,
                    )

                    SearchScope.Reads -> navController.navigateToAdvancedSearch(
                        initialQuery = query,
                        initialSearchKind = AdvancedSearchContract.SearchKind.Reads,
                    )

                    SearchScope.MyNotifications -> navController.navigateToAdvancedSearch(
                        initialQuery = query,
                        initialSearchScope = AdvancedSearchContract.SearchScope.MyNotifications,
                    )
                }
            },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
            onNoteClick = { noteId -> navController.navigateToThread(noteId) },
            onNaddrClick = { naddr -> navController.navigateToArticleDetails(naddr) },
            onSearchContent = { scope, query ->
                val feedSpec = when (scope) {
                    SearchScope.Notes -> buildAdvancedSearchNotesFeedSpec(query = query)
                    SearchScope.Reads -> buildAdvancedSearchReadsFeedSpec(query = query)
                    SearchScope.MyNotifications -> buildAdvancedSearchNotificationsFeedSpec(query = query)
                }
                navController.navigateToExploreFeed(feedSpec = feedSpec)
            },
        ),
    )
}

private fun NavGraphBuilder.advancedSearch(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<AdvancedSearchViewModel>()

    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    AdvancedSearchScreen(
        viewModel = viewModel,
        callbacks = AdvancedSearchContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onNavigateToExploreNoteFeed = { feedSpec, renderType ->
                navController.navigateToExploreFeed(feedSpec, renderType)
            },
            onNavigateToExploreArticleFeed = { feedSpec ->
                navController.navigateToExploreFeed(feedSpec)
            },
        ),
    )
}

private fun NavGraphBuilder.premiumBuying(
    route: String,
    deepLinks: List<NavDeepLink>,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<PremiumBuyingViewModel>()

    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    PremiumBuyingScreen(
        viewModel = viewModel,
        screenCallbacks = PremiumBuyingContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onMoreInfoClick = { tier -> navController.navigateToPremiumMoreInfo(tier.ordinal) },
            onPremiumPurchased = {
                navController.popBackStack()
                navController.navigateToPremiumHome()
            },
        ),
    )
}

private fun NavGraphBuilder.premiumHome(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumHomeViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        PremiumHomeScreen(
            viewModel = viewModel,
            callbacks = PremiumHomeContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
                onRenewSubscription = { navController.navigateToPremiumExtendSubscription(primalName = it) },
                onManagePremium = { navController.navigateToPremiumManage() },
                onLegendCardClick = { navController.navigateToPremiumCard(profileId = it) },
                onSupportPrimal = { navController.navigateToPremiumSupportPrimal() },
                onUpgradeToProClick = { navController.navigateToUpgradeToPrimalPro() },
                onContributePrimal = { navController.navigateToLegendContributePrimal() },
            ),
        )
    }

private fun NavGraphBuilder.premiumSupportPrimal(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<SupportPrimalViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        SupportPrimalScreen(
            viewModel = viewModel,
            callbacks = SupportPrimalContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
                onExtendSubscription = { navController.navigateToPremiumExtendSubscription(primalName = it) },
                onBecomeLegend = { navController.navigateToPremiumBuyPrimalLegend() },
            ),
        )
    }

private fun NavGraphBuilder.premiumLegendContribution(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<LegendContributeViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        LegendContributeScreen(
            viewModel = viewModel,
            callbacks = LegendContributeContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
            ),
        )
    }

private fun NavGraphBuilder.premiumMoreInfo(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val initialTabIndex = it.premiumMoreInfoTabIndex
    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    PremiumMoreInfoScreen(
        initialTabIndex = initialTabIndex ?: 0,
        callbacks = PremiumMoreInfoContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
        ),
    )
}

private fun NavGraphBuilder.premiumBuyPrimalLegend(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<PremiumBecomeLegendViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    PremiumBecomeLegendScreen(
        viewModel = viewModel,
        callbacks = PremiumBecomeLegendContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onLegendPurchased = {
                navController.navigateUp()
                if (it.buyingPremiumFromOrigin == FROM_ORIGIN_PREMIUM_BADGE) {
                    navController.navigateToPremiumHome()
                } else {
                    navController.popBackStack()
                }
            },
        ),
    )
}

private fun NavGraphBuilder.premiumLegendaryProfile(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<LegendaryProfileCustomizationViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        LegendaryProfileCustomizationScreen(
            viewModel = viewModel,
            callbacks = LegendaryProfileCustomizationContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
            ),
        )
    }

private fun NavGraphBuilder.premiumCard(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = dialog(
    route = route,
    arguments = arguments,
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
) {
    val viewModel = hiltViewModel<PremiumCardViewModel>()

    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    PremiumCardScreen(
        viewModel = viewModel,
        callbacks = PremiumCardContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onSeeOtherLegendsClick = { navController.navigateToPremiumLegendLeaderboard() },
            onSeeOtherPrimalOGsClick = { navController.navigateToPremiumOGsLeaderboard() },
            onBecomeLegendClick = {
                navController.navigateToPremiumBuyPrimalLegend(fromOrigin = FROM_ORIGIN_PREMIUM_BADGE)
            },
            onLegendSettingsClick = { navController.navigateToPremiumLegendaryProfile() },
        ),
    )
}

private fun NavGraphBuilder.premiumLegendLeaderboard(
    route: String,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<LegendLeaderboardViewModel>()

    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    LegendLeaderboardScreen(
        viewModel = viewModel,
        callbacks = LegendLeaderboardContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onProfileClick = { navController.navigateToProfile(profileId = it) },
            onAboutLegendsClick = { navController.navigateToPremiumBuyPrimalLegend() },
        ),
    )
}

private fun NavGraphBuilder.premiumOGsLeaderboard(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<OGLeaderboardViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        OGLeaderboardScreen(
            viewModel = viewModel,
            callbacks = OGLeaderboardContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
                onProfileClick = { navController.navigateToProfile(profileId = it) },
                onGetPrimalPremiumClick = { navController.navigateToPremiumBuying() },
            ),
        )
    }

private fun NavGraphBuilder.premiumManage(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumManageViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        PremiumManageScreen(
            viewModel = viewModel,
            callbacks = PremiumManageContract.ScreenCallbacks(
                onFAQClick = { navController.navigateToPremiumMoreInfo(tabIndex = MORE_INFO_FAQ_TAB_INDEX) },
                onClose = { navController.navigateUp() },
                onDestination = {
                    when (it) {
                        PremiumManageContract.ManageDestination.MediaManagement ->
                            navController.navigateToPremiumMediaManagement()

                        PremiumManageContract.ManageDestination.PremiumRelay ->
                            navController.navigateToPremiumRelay()

                        PremiumManageContract.ManageDestination.ContactListBackup ->
                            navController.navigateToPremiumContactList()

                        PremiumManageContract.ManageDestination.ContentBackup ->
                            navController.navigateToPremiumContentBackup()

                        PremiumManageContract.ManageDestination.ManageSubscription ->
                            navController.navigateToPremiumOrderHistory()

                        PremiumManageContract.ManageDestination.ChangePrimalName ->
                            navController.navigateToPremiumChangePrimalName()

                        is PremiumManageContract.ManageDestination.ExtendSubscription ->
                            navController.navigateToPremiumExtendSubscription(primalName = it.primalName)

                        PremiumManageContract.ManageDestination.LegendaryProfileCustomization ->
                            navController.navigateToPremiumLegendaryProfile()

                        PremiumManageContract.ManageDestination.BecomeALegend ->
                            navController.navigateToPremiumBuyPrimalLegend()
                    }
                },
            ),
        )
    }

private fun NavGraphBuilder.premiumContactList(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumContactListViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        PremiumContactListScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.premiumContentBackup(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumContentBackupViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        PremiumContentBackupScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.premiumMediaManagement(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumMediaManagementViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        PremiumMediaManagementScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.premiumChangePrimalName(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumChangePrimalNameViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        PremiumChangePrimalNameScreen(
            onClose = { navController.navigateUp() },
            viewModel = viewModel,
        )
    }

private fun NavGraphBuilder.premiumOrderHistory(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumOrderHistoryViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        PremiumOrderHistoryScreen(
            viewModel = viewModel,
            callbacks = PremiumOrderHistoryContract.ScreenCallbacks(
                onExtendSubscription = { navController.navigateToPremiumExtendSubscription(primalName = it) },
                onClose = { navController.navigateUp() },
            ),
        )
    }

private fun NavGraphBuilder.premiumRelay(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<PremiumRelayViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()

        PremiumRelayScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.messages(
    route: String,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val viewModel = hiltViewModel<MessageConversationListViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    MessageListScreen(
        viewModel = viewModel,
        callbacks = MessageConversationListContract.ScreenCallbacks(
            onConversationClick = { profileId -> navController.navigateToChat(profileId) },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
            onNewMessageClick = { navController.navigateToNewMessage() },
            onClose = { navController.navigateUp() },
        ),
    )
}

private fun NavGraphBuilder.bookmarks(
    route: String,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel: BookmarksViewModel = hiltViewModel()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    BookmarksScreen(
        viewModel = viewModel,
        noteCallbacks = noteCallbacksHandler(navController),
        callbacks = BookmarksContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onGoToWallet = { navController.navigateToWallet() },
        ),
    )
}

private fun NavGraphBuilder.chat(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val viewModel = hiltViewModel<ChatViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ChatScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        noteCallbacks = noteCallbacksHandler(navController),
    )
}

private fun NavGraphBuilder.newMessage(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) { navBackEntry ->
        val viewModel = hiltViewModel<SearchViewModel>(navBackEntry)
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        NewConversationScreen(
            viewModel = viewModel,
            callbacks = NewConversationContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
                onProfileClick = { profileId ->
                    navController.popBackStack()
                    navController.navigateToChat(profileId)
                },
            ),
        )
    }

private fun NavGraphBuilder.notifications(
    route: String,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
    popEnterTransition = {
        when {
            initialState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleIn
        }
    },
    popExitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> mainScreenOut
            else -> primalScaleOut
        }
    },
) { navBackEntry ->
    val viewModel = hiltViewModel<NotificationsViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    NotificationsScreen(
        viewModel = viewModel,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        noteCallbacks = noteCallbacksHandler(navController),
        accountSwitcherCallbacks = accountSwitcherCallbacksHandler(navController = navController),
        callbacks = NotificationsContract.ScreenCallbacks(
            onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.MyNotifications) },
            onGoToWallet = { navController.navigateToWallet() },
            onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
            onNewPostClick = { navController.navigateToNoteEditor(null) },
        ),
    )
}

private fun NavGraphBuilder.thread(
    route: String,
    deepLinks: List<NavDeepLink>,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val viewModel = hiltViewModel<ThreadViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ThreadScreen(
        viewModel = viewModel,
        callbacks = ThreadContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onGoToWallet = { navController.navigateToWallet() },
            onExpandReply = { args -> navController.navigateToNoteEditor(args) },
        ),
        noteCallbacks = noteCallbacksHandler(navController),
    )
}

private fun NavGraphBuilder.articleDetails(
    route: String,
    deepLinks: List<NavDeepLink>,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val viewModel = hiltViewModel<ArticleDetailsViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ArticleDetailsScreen(
        viewModel = viewModel,
        callbacks = ArticleDetailsContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onArticleHashtagClick = { hashtag ->
                navController.navigateToExploreFeed(feedSpec = buildReadsTopicFeedSpec(hashtag = hashtag))
            },
            onGoToWallet = { navController.navigateToWallet() },
        ),
        noteCallbacks = noteCallbacksHandler(navController),
    )
}

private fun NavGraphBuilder.reactions(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val viewModel = hiltViewModel<ReactionsViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ReactionsScreen(
        viewModel = viewModel,
        callbacks = ReactionsContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        ),
    )
}

private fun NavGraphBuilder.media(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { fadeIn() },
    exitTransition = { fadeOut() },
    popEnterTransition = { EnterTransition.None },
    popExitTransition = { fadeOut() },
) { navBackEntry ->
    val viewModel = hiltViewModel<EventMediaGalleryViewModel>(navBackEntry)
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        UnlockScreenOrientation()
        EventMediaGalleryScreen(
            onClose = { navController.navigateUp() },
            viewModel = viewModel,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun NavGraphBuilder.mediaItem(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) {
    val viewModel = hiltViewModel<MediaItemViewModel>()

    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        UnlockScreenOrientation()
        MediaItemScreen(
            onClose = { navController.navigateUp() },
            viewModel = viewModel,
        )
    }
}

private fun NavGraphBuilder.profile(
    route: String,
    arguments: List<NamedNavArgument>,
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val streamState = LocalStreamState.current
    val viewModel = hiltViewModel<ProfileDetailsViewModel>(it)

    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ProfileDetailsScreen(
        viewModel = viewModel,
        callbacks = ProfileDetailsContract.ScreenCallbacks(
            onClose = { navController.navigateUp() },
            onEditProfileClick = { navController.navigateToProfileEditor() },
            onMessageClick = { profileId -> navController.navigateToChat(profileId = profileId) },
            onSendWalletTx = { transaction -> navController.navigateToWalletCreateTransaction(transaction) },
            onDrawerQrCodeClick = { profileId -> navController.navigateToProfileQrCodeViewer(profileId) },
            onFollowsClick = { profileId, followsType ->
                navController.navigateToProfileFollows(
                    profileId = profileId,
                    followsType = followsType,
                )
            },
            onMediaItemClick = { navController.navigateToMediaItem(it) },
            onGoToWallet = { navController.navigateToWallet() },
            onSearchClick = { navController.navigateToAdvancedSearch(initialPostedBy = listOf(it)) },
            onPremiumBadgeClick = { premiumTier, profileId ->
                if (premiumTier.isPrimalLegendTier() || premiumTier.isPremiumTier()) {
                    navController.navigateToPremiumCard(profileId = profileId)
                }
            },
            onNewPostClick = { navController.navigateToNoteEditor(null) },
            onLiveStreamClick = { naddr -> streamState.start(naddr) },
        ),
        noteCallbacks = noteCallbacksHandler(navController),
    )
}

private fun NavGraphBuilder.profileEditor(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<ProfileEditorViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        ProfileEditorScreen(
            viewModel = viewModel,
            callbacks = ProfileEditorContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
                onNavigateToPremiumBuying = { navController.navigateToPremiumBuying() },
            ),
        )
    }

private fun NavGraphBuilder.profileFollows(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<ProfileFollowsViewModel>(it)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ProfileFollowsScreen(
        viewModel = viewModel,
        callbacks = ProfileFollowsContract.ScreenCallbacks(
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
            onClose = { navController.navigateUp() },
        ),
    )
}

private fun NavGraphBuilder.profileQrCodeViewer(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val streamState = LocalStreamState.current
    val viewModel = hiltViewModel<ProfileQrCodeViewModel>()
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        LockToOrientationPortrait()
        ProfileQrCodeViewerScreen(
            viewModel = viewModel,
            callbacks = ProfileQrCodeContract.ScreenCallbacks(
                onClose = { navController.navigateUp() },
                onProfileScan = { profileId ->
                    navController.popBackStack()
                    navController.navigateToProfile(profileId)
                },
                onNoteScan = { noteId ->
                    navController.popBackStack()
                    navController.navigateToThread(noteId)
                },
                onLiveStreamScan = { naddr ->
                    streamState.start(naddr)
                    navController.popBackStack()
                },
                onArticleScan = { naddr ->
                    navController.popBackStack()
                    navController.navigateToArticleDetails(naddr)
                },
                onDraftTxScan = { draftTx ->
                    navController.popBackStack()
                    navController.navigateToWalletCreateTransaction(draftTx)
                },
                onPromoCodeScan = {
                    navController.popBackStack()
                    navController.navigateToScanCode(it)
                },
            ),
        )
    }
}

private fun NavGraphBuilder.logout(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = dialog(
    route = route,
    arguments = arguments,
) {
    val viewModel: LogoutViewModel = hiltViewModel(it)
    LockToOrientationPortrait()
    LogoutScreen(
        viewModel = viewModel,
        callbacks = LogoutContract.ScreenCallbacks(
            onClose = { navController.popBackStack() },
            navigateToHome = { navController.navigateToHome() },
            navigateToWelcome = { navController.navigateToWelcome() },
        ),
    )
}
