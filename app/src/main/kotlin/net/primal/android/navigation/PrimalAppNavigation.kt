package net.primal.android.navigation

import android.net.Uri
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
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.attachments.gallery.MediaGalleryScreen
import net.primal.android.attachments.gallery.MediaGalleryViewModel
import net.primal.android.auth.create.CreateAccountViewModel
import net.primal.android.auth.create.ui.CreateAccountScreen
import net.primal.android.auth.login.LoginScreen
import net.primal.android.auth.login.LoginViewModel
import net.primal.android.auth.logout.LogoutScreen
import net.primal.android.auth.logout.LogoutViewModel
import net.primal.android.auth.welcome.WelcomeScreen
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.findActivity
import net.primal.android.discuss.feed.FeedScreen
import net.primal.android.discuss.feed.FeedViewModel
import net.primal.android.discuss.list.FeedListScreen
import net.primal.android.discuss.list.FeedListViewModel
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.editor.NoteEditorViewModel
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
import net.primal.android.notifications.list.NotificationsScreen
import net.primal.android.notifications.list.NotificationsViewModel
import net.primal.android.profile.details.ProfileScreen
import net.primal.android.profile.details.ProfileViewModel
import net.primal.android.profile.edit.EditProfileScreen
import net.primal.android.profile.edit.EditProfileViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.thread.ThreadScreen
import net.primal.android.thread.ThreadViewModel

private fun NavController.navigateToWelcome() =
    navigate(
        route = "welcome",
        navOptions = navOptions { clearBackStack() },
    )

private fun NavController.navigateToLogin() = navigate(route = "login")

private fun NavController.navigateToCreate() = navigate(route = "create_account")

private fun NavController.navigateToLogout() = navigate(route = "logout")

private fun NavController.navigateToFeedList() = navigate(route = "feed/list")

private fun NavController.navigateToSearch() = navigate(route = "search")

private fun NavController.navigateToNoteEditor(
    preFillContent: String? = null,
    preFillFileUri: Uri? = null,
    replyToNoteId: String? = null,
) {
    val route = "editor" +
        "?$NEW_POST_REPLY_TO_NOTE_ID=${replyToNoteId.orEmpty()}" +
        "&$NEW_POST_PRE_FILL_FILE_URI=${preFillFileUri?.toString().orEmpty().asUrlEncoded()}" +
        "&$NEW_POST_PRE_FILL_CONTENT=${preFillContent.orEmpty().asBase64Encoded()}"
    navigate(route = route)
}

private val NavController.topLevelNavOptions: NavOptions
    @SuppressWarnings("RestrictedApi")
    get() {
        val feedDestination = currentBackStack.value.find {
            it.destination.route?.contains("feed") == true
        }
        return navOptions {
            popUpTo(id = feedDestination?.destination?.id ?: 0)
        }
    }

private fun NavController.navigateToFeed(directive: String) =
    navigate(
        route = "feed?directive=${directive.asUrlEncoded()}",
        navOptions = navOptions { clearBackStack() },
    )

private fun NavController.navigateToExplore() =
    navigate(
        route = "explore",
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

private fun NavController.navigateToEditProfile() = navigate(route = "edit_profile")

private fun NavController.navigateToSettings() = navigate(route = "settings")

private fun NavController.navigateToWallet(nwcUrl: String? = null) =
    when {
        nwcUrl != null -> navigate(route = "wallet_settings?nwcUrl=$nwcUrl")
        else -> navigate(route = "wallet_settings")
    }

private fun NavController.navigateToThread(noteId: String) = navigate(route = "thread/$noteId")

private fun NavController.navigateToMediaGallery(noteId: String, mediaUrl: String) =
    navigate(route = "media/$noteId?$MEDIA_URL=$mediaUrl")

private fun NavController.navigateToExploreFeed(query: String) = navigate(route = "explore/$query")

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun PrimalAppNavigation() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    val topLevelDestinationHandler: (PrimalTopLevelDestination) -> Unit = {
        when (it) {
            PrimalTopLevelDestination.Feed -> navController.popBackStack()
            PrimalTopLevelDestination.Explore -> navController.navigateToExplore()
            PrimalTopLevelDestination.Messages -> navController.navigateToMessages()
            PrimalTopLevelDestination.Notifications -> navController.navigateToNotifications()
        }
    }

    val drawerDestinationHandler: (DrawerScreenDestination) -> Unit = {
        when (it) {
            DrawerScreenDestination.Profile -> navController.navigateToProfile()
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
                            navController.navigateToFeed(directive = it.userPubkey)
                        }

                        is DeepLink.NostrWalletConnect -> {
                            navController.popBackStack()
                            navController.navigateToWallet(
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

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = AppTheme.shapes.medium,
    ) {
        NavHost(
            navController = navController,
            startDestination = "splash",
        ) {
            splash(route = "splash")

            welcome(route = "welcome", navController = navController)

            login(route = "login", navController = navController)

            createAccount(route = "create_account", navController = navController)

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

            explore(
                route = "explore",
                navController = navController,
                onTopLevelDestinationChanged = topLevelDestinationHandler,
                onDrawerScreenClick = drawerDestinationHandler,
            )

            exploreFeed(
                route = "explore/{$SEARCH_QUERY}",
                arguments = listOf(
                    navArgument(SEARCH_QUERY) {
                        type = NavType.StringType
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

            feedList(
                route = "feed/list",
                navController = navController,
            )

            noteEditor(
                route = "editor" +
                    "?$NEW_POST_REPLY_TO_NOTE_ID={$NEW_POST_REPLY_TO_NOTE_ID}" +
                    "&$NEW_POST_PRE_FILL_FILE_URI={$NEW_POST_PRE_FILL_FILE_URI}" +
                    "&$NEW_POST_PRE_FILL_CONTENT={$NEW_POST_PRE_FILL_CONTENT}",
                arguments = listOf(
                    navArgument(NEW_POST_REPLY_TO_NOTE_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(NEW_POST_PRE_FILL_FILE_URI) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(NEW_POST_PRE_FILL_CONTENT) {
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

            media(
                route = "media/{$NOTE_ID}?$MEDIA_URL={$MEDIA_URL}",
                arguments = listOf(
                    navArgument(NOTE_ID) {
                        type = NavType.StringType
                    },
                    navArgument(MEDIA_URL) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
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

            editProfile(route = "edit_profile", navController = navController)

            settingsNavigation(route = "settings", navController = navController)
        }
    }
}

private fun NavGraphBuilder.splash(route: String) =
    composable(route = route) {
        SplashScreen()
    }

private fun NavGraphBuilder.welcome(route: String, navController: NavController) =
    composable(route = route) {
        LockToOrientationPortrait()
        PrimalTheme(PrimalTheme.Sunset) {
            WelcomeScreen(
                onSignInClick = { navController.navigateToLogin() },
                onCreateAccountClick = { navController.navigateToCreate() },
            )
        }
    }

private fun NavGraphBuilder.login(route: String, navController: NavController) =
    composable(route = route) {
        val viewModel: LoginViewModel = hiltViewModel(it)
        PrimalTheme(PrimalTheme.Sunset) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { pubkey -> navController.navigateToFeed(pubkey) },
                onClose = { navController.popBackStack() },
            )
        }
    }

private fun NavGraphBuilder.createAccount(route: String, navController: NavController) =
    composable(route = route) {
        val viewModel: CreateAccountViewModel = hiltViewModel(it)
        PrimalTheme(PrimalTheme.Sunset) {
            CreateAccountScreen(
                viewModel = viewModel,
                onCreateSuccess = { pubkey -> navController.navigateToFeed(pubkey) },
                onClose = { navController.popBackStack() },
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
    arguments = arguments,
) { navBackEntry ->
    val viewModel = hiltViewModel<FeedViewModel>(navBackEntry)
    LockToOrientationPortrait()
    FeedScreen(
        viewModel = viewModel,
        onFeedsClick = { navController.navigateToFeedList() },
        onNewPostClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent) },
        onPostClick = { postId -> navController.navigateToThread(noteId = postId) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(replyToNoteId = postId) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onMediaClick = { noteId, mediaUrl ->
            navController.navigateToMediaGallery(
                noteId = noteId,
                mediaUrl = mediaUrl,
            )
        },
        onWalletUnavailable = { navController.navigateToWallet() },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
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
    val viewModel = hiltViewModel<NoteEditorViewModel>(it)
    LockToOrientationPortrait()
    NoteEditorScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class)
private fun NavGraphBuilder.feedList(route: String, navController: NavController) =
    bottomSheet(
        route = route,
    ) {
        val viewModel = hiltViewModel<FeedListViewModel>(it)
        LockToOrientationPortrait()
        FeedListScreen(
            viewModel = viewModel,
            onFeedSelected = { directive -> navController.navigateToFeed(directive = directive) },
        )
    }

private fun NavGraphBuilder.explore(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
) {
    val viewModel = hiltViewModel<ExploreHomeViewModel>(it)
    LockToOrientationPortrait()
    ExploreHomeScreen(
        viewModel = viewModel,
        onHashtagClick = { query -> navController.navigateToExploreFeed(query = query) },
        onSearchClick = { navController.navigateToSearch() },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
    )
}

private fun NavGraphBuilder.exploreFeed(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) {
    val viewModel = hiltViewModel<ExploreFeedViewModel>(it)
    LockToOrientationPortrait()
    ExploreFeedScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(postId) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(replyToNoteId = postId) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onMediaClick = { noteId, mediaUrl ->
            navController.navigateToMediaGallery(
                noteId = noteId,
                mediaUrl = mediaUrl,
            )
        },
        onWalletUnavailable = { navController.navigateToWallet() },
    )
}

private fun NavGraphBuilder.search(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<SearchViewModel>(it)
        LockToOrientationPortrait()
        SearchScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
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
) { navBackEntry ->
    val viewModel = hiltViewModel<MessageConversationListViewModel>(navBackEntry)
    LockToOrientationPortrait()
    MessageListScreen(
        viewModel = viewModel,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        onConversationClick = { profileId -> navController.navigateToChat(profileId) },
        onNewMessageClick = { navController.navigateToNewMessage() },
    )
}

private fun NavGraphBuilder.chat(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) { navBackEntry ->
    val viewModel = hiltViewModel<ChatViewModel>(navBackEntry)
    LockToOrientationPortrait()
    ChatScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onNoteClick = { noteId -> navController.navigateToThread(noteId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(hashtag) },
        onMediaClick = { noteId, mediaUrl ->
            navController.navigateToMediaGallery(
                noteId = noteId,
                mediaUrl = mediaUrl,
            )
        },
    )
}

private fun NavGraphBuilder.newMessage(route: String, navController: NavController) =
    composable(
        route = route,
    ) { navBackEntry ->
        val viewModel = hiltViewModel<SearchViewModel>(navBackEntry)
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
) { navBackEntry ->
    val viewModel = hiltViewModel<NotificationsViewModel>(navBackEntry)
    LockToOrientationPortrait()
    NotificationsScreen(
        viewModel = viewModel,
        onProfileClick = { navController.navigateToProfile(profileId = it) },
        onNoteClick = { navController.navigateToThread(noteId = it) },
        onNoteReplyClick = { noteId -> navController.navigateToNoteEditor(replyToNoteId = noteId) },
        onHashtagClick = { navController.navigateToExploreFeed(query = it) },
        onMediaClick = { noteId, mediaUrl ->
            navController.navigateToMediaGallery(
                noteId = noteId,
                mediaUrl = mediaUrl,
            )
        },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent) },
        onNotificationSettings = { navController.navigateToNotificationsSettings() },
        onWalletUnavailable = { navController.navigateToWallet() },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
    )
}

private fun NavGraphBuilder.thread(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) { navBackEntry ->
    val viewModel = hiltViewModel<ThreadViewModel>(navBackEntry)
    LockToOrientationPortrait()
    ThreadScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(postId) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(replyToNoteId = postId) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onMediaClick = { noteId, mediaUrl ->
            navController.navigateToMediaGallery(
                noteId = noteId,
                mediaUrl = mediaUrl,
            )
        },
        onWalletUnavailable = { navController.navigateToWallet() },
        onReplyInNoteEditor = { replyToId, uri, text ->
            navController.navigateToNoteEditor(
                replyToNoteId = replyToId,
                preFillContent = text,
                preFillFileUri = uri,
            )
        },
    )
}

private fun NavGraphBuilder.media(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) { navBackEntry ->
    val viewModel = hiltViewModel<MediaGalleryViewModel>(navBackEntry)
    MediaGalleryScreen(
        onClose = { navController.navigateUp() },
        viewModel = viewModel,
    )
}

private fun NavGraphBuilder.profile(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) {
    val viewModel = hiltViewModel<ProfileViewModel>(it)

    LockToOrientationPortrait()
    ProfileScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(noteId = postId) },
        onPostReplyClick = { postId -> navController.navigateToNoteEditor(replyToNoteId = postId) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNoteEditor(preFillContent) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onEditProfileClick = { navController.navigateToEditProfile() },
        onMessageClick = { profileId -> navController.navigateToChat(profileId = profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onMediaClick = { noteId, mediaUrl ->
            navController.navigateToMediaGallery(
                noteId = noteId,
                mediaUrl = mediaUrl,
            )
        },
        onWalletUnavailable = { navController.navigateToWallet() },
    )
}

private fun NavGraphBuilder.editProfile(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<EditProfileViewModel>()

        LockToOrientationPortrait()
        EditProfileScreen(viewModel = viewModel, onClose = { navController.navigateUp() })
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
