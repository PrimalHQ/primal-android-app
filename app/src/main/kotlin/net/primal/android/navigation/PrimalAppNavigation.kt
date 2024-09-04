package net.primal.android.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.articles.reads.ReadsScreen
import net.primal.android.articles.reads.ReadsViewModel
import net.primal.android.attachments.gallery.MediaGalleryScreen
import net.primal.android.attachments.gallery.MediaGalleryViewModel
import net.primal.android.auth.login.LoginScreen
import net.primal.android.auth.login.LoginViewModel
import net.primal.android.auth.logout.LogoutScreen
import net.primal.android.auth.logout.LogoutViewModel
import net.primal.android.auth.onboarding.account.OnboardingViewModel
import net.primal.android.auth.onboarding.account.ui.OnboardingScreen
import net.primal.android.auth.onboarding.wallet.OnboardingWalletActivation
import net.primal.android.auth.welcome.WelcomeScreen
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.findActivity
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.editor.di.noteEditorViewModel
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.editor.domain.NoteEditorArgs.Companion.asNoteEditorArgs
import net.primal.android.editor.ui.NoteEditorScreen
import net.primal.android.explore.feed.ExploreFeedScreen
import net.primal.android.explore.feed.ExploreFeedViewModel
import net.primal.android.explore.home.ExploreHomeScreen
import net.primal.android.explore.home.ExploreHomeViewModel
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.explore.search.ui.SearchScreen
import net.primal.android.messages.chat.ChatScreen
import net.primal.android.messages.chat.ChatViewModel
import net.primal.android.messages.conversation.MessageConversationListViewModel
import net.primal.android.messages.conversation.MessageListScreen
import net.primal.android.messages.conversation.create.NewConversationScreen
import net.primal.android.navigation.deeplinking.DeepLink
import net.primal.android.navigation.deeplinking.ext.handleDeeplink
import net.primal.android.navigation.splash.SplashContract
import net.primal.android.navigation.splash.SplashScreen
import net.primal.android.navigation.splash.SplashViewModel
import net.primal.android.note.reactions.ReactionsScreen
import net.primal.android.note.reactions.ReactionsViewModel
import net.primal.android.notes.FeedScreen
import net.primal.android.notes.FeedViewModel
import net.primal.android.notes.home.HomeFeedScreen
import net.primal.android.notes.home.HomeFeedViewModel
import net.primal.android.notifications.list.NotificationsScreen
import net.primal.android.notifications.list.NotificationsViewModel
import net.primal.android.profile.details.ProfileDetailsViewModel
import net.primal.android.profile.details.ui.ProfileDetailsScreen
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.profile.editor.ProfileEditorViewModel
import net.primal.android.profile.editor.ui.ProfileEditorScreen
import net.primal.android.profile.follows.ProfileFollowsScreen
import net.primal.android.profile.follows.ProfileFollowsViewModel
import net.primal.android.profile.qr.ProfileQrCodeViewModel
import net.primal.android.profile.qr.ui.ProfileQrCodeViewerScreen
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.thread.articles.ArticleDetailsScreen
import net.primal.android.thread.articles.ArticleDetailsViewModel
import net.primal.android.thread.notes.ThreadScreen
import net.primal.android.thread.notes.ThreadViewModel
import net.primal.android.wallet.activation.WalletActivationViewModel

private fun NavController.navigateToWelcome() =
    navigate(
        route = "welcome",
        navOptions = navOptions { clearBackStack() },
    )

private fun NavController.navigateToLogin() = navigate(route = "login")

private fun NavController.navigateToOnboarding() = navigate(route = "onboarding")

private fun NavController.navigateToWalletOnboarding() = navigate(route = "onboardingWallet")

private fun NavController.navigateToLogout() = navigate(route = "logout")

private fun NavController.navigateToSearch() = navigate(route = "search")

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

// @Deprecated("It's going to be deleted.")
// fun NavController.navigateToFeed(directive: String? = null) =
//    navigate(
//        route = when (directive) {
//            null -> "feed"
//            else -> "feed?$FEED_DIRECTIVE=${directive.asUrlEncoded()}"
//        },
//        navOptions = navOptions { clearBackStack() },
//    )

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

private fun NavController.navigateToWallet() =
    navigate(
        route = "wallet",
        navOptions = topLevelNavOptions,
    )

private fun NavController.navigateToMessages() =
    navigate(
        route = "messages",
        navOptions = topLevelNavOptions,
    )

private fun NavController.navigateToChat(profileId: String) =
    navigate(
        route = "messages/$profileId",
    )

private fun NavController.navigateToNewMessage() = navigate(route = "messages/new")

private fun NavController.navigateToNotifications() =
    navigate(
        route = "notifications",
        navOptions = topLevelNavOptions,
    )

fun NavController.navigateToProfile(profileId: String? = null) =
    when {
        profileId != null -> navigate(route = "profile?$PROFILE_ID=$profileId")
        else -> navigate(route = "profile")
    }

fun NavController.navigateToProfileQrCodeViewer(profileId: String? = null) =
    when {
        profileId != null -> navigate(route = "profileQrCodeViewer?$PROFILE_ID=$profileId")
        else -> navigate(route = "profileQrCodeViewer")
    }

fun NavController.navigateToProfileFollows(profileId: String, followsType: ProfileFollowsType) =
    navigate(route = "profile/$profileId/follows?$FOLLOWS_TYPE=$followsType")

fun NavController.navigateToProfileEditor() = navigate(route = "profileEditor")

private fun NavController.navigateToSettings() = navigate(route = "settings")

private fun NavController.navigateToWalletSettings(nwcUrl: String? = null) =
    when {
        nwcUrl != null -> navigate(route = "wallet_settings?nwcUrl=$nwcUrl")
        else -> navigate(route = "wallet_settings")
    }

fun NavController.navigateToThread(noteId: String) = navigate(route = "thread/$noteId")

fun NavController.navigateToArticleDetails(naddr: String) = navigate(route = "article/$naddr")

fun NavController.navigateToNoteReactions(noteId: String) = navigate(route = "reactions/$noteId")

fun NavController.navigateToMediaGallery(
    noteId: String,
    mediaUrl: String,
    mediaPositionMs: Long = 0,
) = navigate(
    route = "media/$noteId" +
        "?$MEDIA_URL=$mediaUrl" +
        "&$MEDIA_POSITION_MS=$mediaPositionMs",
)

fun NavController.navigateToExploreFeed(query: String) =
    navigate(route = "explore?$EXPLORE_FEED_DIRECTIVE=${"search;$query".asBase64Encoded()}")

private fun NavController.navigateToBookmarks(userId: String) =
    navigate(route = "explore?$EXPLORE_FEED_DIRECTIVE=${"bookmarks;$userId".asBase64Encoded()}")

@Composable
fun PrimalAppNavigation() {
    val navController = rememberNavController()

    val topLevelDestinationHandler: (PrimalTopLevelDestination) -> Unit = {
        when (it) {
            PrimalTopLevelDestination.Home -> navController.popBackStack()
            PrimalTopLevelDestination.Reads -> navController.navigateToReads()
            PrimalTopLevelDestination.Wallet -> navController.navigateToWallet()
            PrimalTopLevelDestination.Messages -> navController.navigateToMessages()
            PrimalTopLevelDestination.Notifications -> navController.navigateToNotifications()
        }
    }

    val drawerDestinationHandler: (DrawerScreenDestination) -> Unit = {
        when (it) {
            DrawerScreenDestination.Profile -> navController.navigateToProfile()
            is DrawerScreenDestination.Bookmarks -> navController.navigateToBookmarks(userId = it.userId)
            DrawerScreenDestination.Settings -> navController.navigateToSettings()
            DrawerScreenDestination.SignOut -> navController.navigateToLogout()
        }
    }

    val splashViewModel: SplashViewModel = hiltViewModel()
    val context = LocalContext.current
    LaunchedEffect(navController, splashViewModel) {
        splashViewModel.effect.collect {
            when (it) {
                SplashContract.SideEffect.NoActiveAccount -> navController.navigateToWelcome()
                is SplashContract.SideEffect.ActiveAccount -> {
                    val activity = context.findActivity()

                    val url = activity?.intent?.data?.toString()?.ifBlank { null }

                    when (url.handleDeeplink()) {
                        is DeepLink.Profile, is DeepLink.Note, null -> {
                            navController.navigateToHome()
                        }

                        is DeepLink.NostrWalletConnect -> {
                            navController.popBackStack()
                            navController.navigateToWalletSettings(
                                nwcUrl = withContext(Dispatchers.IO) {
                                    URLEncoder.encode(url, Charsets.UTF_8.name())
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
    ) {
        splash(route = "splash")

        welcome(route = "welcome", navController = navController)

        login(route = "login", navController = navController)

        onboarding(route = "onboarding", navController = navController)

        onboardingWalletActivation(route = "onboardingWallet", navController)

        logout(route = "logout", navController = navController)

        feed(
            route = "feed?$FEED_DIRECTIVE={$FEED_DIRECTIVE}",
            arguments = listOf(
                navArgument(FEED_DIRECTIVE) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
        )

        home(
            route = "home",
            arguments = emptyList(),
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
        )

        reads(
            route = "reads",
            arguments = emptyList(),
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
        )

        exploreFeed(
            route = "explore?$EXPLORE_FEED_DIRECTIVE={$EXPLORE_FEED_DIRECTIVE}",
            arguments = listOf(
                navArgument(EXPLORE_FEED_DIRECTIVE) {
                    type = NavType.StringType
                    nullable = false
                },
            ),
            navController = navController,
        )

        search(
            route = "search",
            navController = navController,
        )

        messages(
            route = "messages",
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
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

        newMessage(
            route = "messages/new",
            navController = navController,
        )

        notifications(
            route = "notifications",
            navController = navController,
            onTopLevelDestinationChanged = topLevelDestinationHandler,
            onDrawerScreenClick = drawerDestinationHandler,
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
            navController = navController,
        )

        thread(
            route = "thread/{$NOTE_ID}",
            arguments = listOf(
                navArgument(NOTE_ID) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )

        articleDetails(
            route = "article/{$NADDR}",
            arguments = listOf(
                navArgument(NADDR) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )

        noteReactions(
            route = "reactions/{$NOTE_ID}",
            arguments = listOf(
                navArgument(NOTE_ID) {
                    type = NavType.StringType
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

        profile(
            route = "profile?$PROFILE_ID={$PROFILE_ID}",
            arguments = listOf(
                navArgument(PROFILE_ID) {
                    type = NavType.StringType
                    nullable = true
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

private fun NavGraphBuilder.splash(route: String) =
    composable(route = route) {
        SplashScreen()
    }

private fun NavGraphBuilder.welcome(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = {
            when (initialState.destination.route) {
                "login", "onboarding" -> slideInHorizontally(initialOffsetX = { -it })
                else -> null
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                "login", "onboarding" -> slideOutHorizontally(targetOffsetX = { -it })
                else -> null
            }
        },
    ) {
        LockToOrientationPortrait()
        PrimalTheme(PrimalTheme.Sunset) {
            ApplyEdgeToEdge(isDarkTheme = true)
            WelcomeScreen(
                onSignInClick = { navController.navigateToLogin() },
                onCreateAccountClick = { navController.navigateToOnboarding() },
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
                onLoginSuccess = { navController.navigateToHome() },
                onClose = { navController.popBackStack() },
            )
        }
    }

private fun NavGraphBuilder.onboarding(route: String, navController: NavController) =
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
                "onboardingWallet" -> slideOutHorizontally(targetOffsetX = { -it })
                else -> null
            }
        },
    ) {
        val viewModel: OnboardingViewModel = hiltViewModel(it)
        LockToOrientationPortrait()
        PrimalTheme(PrimalTheme.Sunset) {
            ApplyEdgeToEdge(isDarkTheme = true)
            OnboardingScreen(
                viewModel = viewModel,
                onClose = { navController.popBackStack() },
                onOnboarded = { navController.navigateToHome() },
                onActivateWallet = { navController.navigateToWalletOnboarding() },
            )
        }
    }

private fun NavGraphBuilder.onboardingWalletActivation(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
    ) {
        val viewModel = hiltViewModel<WalletActivationViewModel>()
        PrimalTheme(PrimalTheme.Sunset) {
            ApplyEdgeToEdge(isDarkTheme = true)
            OnboardingWalletActivation(
                viewModel = viewModel,
                onDoneOrDismiss = { navController.navigateToHome() },
            )
        }
    }

private fun NavGraphBuilder.feed(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() ->
                scaleOut(animationSpec = tween(), targetScale = 1.0f)

            else -> primalScaleOut
        }
    },
    popEnterTransition = {
        when {
            initialState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleIn
        }
    },
    popExitTransition = { primalScaleOut },
    arguments = arguments,
) { navBackEntry ->
    val viewModel = hiltViewModel<FeedViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    FeedScreen(
        viewModel = viewModel,
        onFeedClick = { /*directive -> navController.navigateToFeed(directive = directive)*/ },
        onNewPostClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent?.asNoteEditorArgs()) },
        onPostClick = { postId -> navController.navigateToThread(noteId = postId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = postId)) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
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
        onGoToWallet = { navController.navigateToWallet() },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
        onSearchClick = { navController.navigateToSearch() },
    )
}

private fun NavGraphBuilder.home(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> null
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
            targetState.destination.route.isMainScreenRoute() -> null
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
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
        onPostClick = { postId -> navController.navigateToThread(noteId = postId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = postId)) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
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
        onGoToWallet = { navController.navigateToWallet() },
        onSearchClick = { navController.navigateToSearch() },
    )
}

private fun NavGraphBuilder.reads(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> null
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
            targetState.destination.route.isMainScreenRoute() -> null
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
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
        onSearchClick = { navController.navigateToSearch() },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr) },
    )
}

private fun NavGraphBuilder.noteEditor(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) {
    val viewModel = noteEditorViewModel(
        args = it.arguments?.getString(NOTE_EDITOR_ARGS)
            ?.asBase64Decoded()
            ?.asNoteEditorArgs(),
    )
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    NoteEditorScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.explore(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> null
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
            targetState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleOut
        }
    },
) {
    val viewModel = hiltViewModel<ExploreHomeViewModel>(it)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ExploreHomeScreen(
        viewModel = viewModel,
        onHashtagClick = { query -> navController.navigateToExploreFeed(query = query) },
        onSearchClick = { navController.navigateToSearch() },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
    )
}

private fun NavGraphBuilder.exploreFeed(
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
    val viewModel = hiltViewModel<ExploreFeedViewModel>(it)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ExploreFeedScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(postId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = postId)) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent.asNoteEditorArgs()) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
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
        onGoToWallet = { navController.navigateToWallet() },
    )
}

private fun NavGraphBuilder.search(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<SearchViewModel>(it)
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        SearchScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
            onNoteClick = { noteId -> navController.navigateToThread(noteId) },
            onNaddrClick = { naddr -> navController.navigateToArticleDetails(naddr) },
            onSearchContent = { query -> navController.navigateToExploreFeed(query) },
        )
    }

private fun NavGraphBuilder.messages(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> null
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
            targetState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleOut
        }
    },
) { navBackEntry ->
    val viewModel = hiltViewModel<MessageConversationListViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    MessageListScreen(
        viewModel = viewModel,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        onConversationClick = { profileId -> navController.navigateToChat(profileId) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onNewMessageClick = { navController.navigateToNewMessage() },
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
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
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onNoteClick = { noteId -> navController.navigateToThread(noteId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(hashtag) },
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
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId ->
                navController.popBackStack()
                navController.navigateToChat(profileId)
            },
        )
    }

private fun NavGraphBuilder.notifications(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
    enterTransition = { null },
    exitTransition = {
        when {
            targetState.destination.route.isMainScreenRoute() -> null
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
            targetState.destination.route.isMainScreenRoute() -> null
            else -> primalScaleOut
        }
    },
) { navBackEntry ->
    val viewModel = hiltViewModel<NotificationsViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    NotificationsScreen(
        viewModel = viewModel,
        onProfileClick = { navController.navigateToProfile(profileId = it) },
        onNoteClick = { navController.navigateToThread(noteId = it) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onNoteReplyClick = { noteId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = noteId)) },
        onHashtagClick = { navController.navigateToExploreFeed(query = it) },
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
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent.asNoteEditorArgs()) },
        onNotificationSettings = { navController.navigateToNotificationsSettings() },
        onGoToWallet = { navController.navigateToWallet() },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
    )
}

private fun NavGraphBuilder.thread(
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
    val viewModel = hiltViewModel<ThreadViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ThreadScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(postId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = postId)) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent.asNoteEditorArgs()) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
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
        onGoToWallet = { navController.navigateToWallet() },
        onExpandReply = { args -> navController.navigateToNoteEditor(args) },
        onReactionsClick = { noteId -> navController.navigateToNoteReactions(noteId = noteId) },
    )
}

private fun NavGraphBuilder.articleDetails(
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
    val viewModel = hiltViewModel<ArticleDetailsViewModel>(navBackEntry)
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ArticleDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onNoteClick = { postId -> navController.navigateToThread(postId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr) },
        onArticleCommentClick = { naddr ->
            navController.navigateToNoteEditor(
                NoteEditorArgs(replyToArticleNaddr = naddr),
            )
        },
        onNoteReplyClick = { postId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = postId)) },
        onNoteQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent.asNoteEditorArgs()) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
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
        onGoToWallet = { navController.navigateToWallet() },
        onReactionsClick = { noteId -> navController.navigateToNoteReactions(noteId = noteId) },
    )
}

private fun NavGraphBuilder.noteReactions(
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
        onClose = { navController.navigateUp() },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
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
    val viewModel = hiltViewModel<MediaGalleryViewModel>(navBackEntry)
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        MediaGalleryScreen(
            onClose = { navController.navigateUp() },
            viewModel = viewModel,
        )
    }
}

private fun NavGraphBuilder.profile(
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
    val viewModel = hiltViewModel<ProfileDetailsViewModel>(it)

    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    ProfileDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(noteId = postId) },
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr = naddr) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(NoteEditorArgs(replyToNoteId = postId)) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent.asNoteEditorArgs()) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onEditProfileClick = { navController.navigateToProfileEditor() },
        onMessageClick = { profileId -> navController.navigateToChat(profileId = profileId) },
        onZapProfileClick = { transaction -> navController.navigateToWalletCreateTransaction(transaction) },
        onDrawerQrCodeClick = { profileId -> navController.navigateToProfileQrCodeViewer(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onFollowsClick = { profileId, followsType ->
            navController.navigateToProfileFollows(
                profileId = profileId,
                followsType = followsType,
            )
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
        onGoToWallet = { navController.navigateToWallet() },
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
        ProfileEditorScreen(viewModel = viewModel, onClose = { navController.navigateUp() })
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
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onClose = { navController.navigateUp() },
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
    val viewModel = hiltViewModel<ProfileQrCodeViewModel>()
    LockToOrientationPortrait()
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        ApplyEdgeToEdge(isDarkTheme = true)
        ProfileQrCodeViewerScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onProfileScan = { profileId ->
                navController.popBackStack()
                navController.navigateToProfile(profileId)
            },
            onNoteScan = { noteId ->
                navController.popBackStack()
                navController.navigateToThread(noteId)
            },
            onDraftTxScan = { draftTx ->
                navController.popBackStack()
                navController.navigateToWalletCreateTransaction(draftTx)
            },
        )
    }
}

private fun NavGraphBuilder.logout(route: String, navController: NavController) =
    dialog(
        route = route,
    ) {
        val viewModel: LogoutViewModel = hiltViewModel(it)
        LockToOrientationPortrait()
        LogoutScreen(
            viewModel = viewModel,
            onClose = { navController.popBackStack() },
        )
    }
