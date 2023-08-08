package net.primal.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
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
import net.primal.android.R
import net.primal.android.auth.login.LoginScreen
import net.primal.android.auth.login.LoginViewModel
import net.primal.android.auth.logout.LogoutScreen
import net.primal.android.auth.logout.LogoutViewModel
import net.primal.android.auth.welcome.WelcomeScreen
import net.primal.android.core.compose.DemoPrimaryScreen
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.discuss.feed.FeedScreen
import net.primal.android.discuss.feed.FeedViewModel
import net.primal.android.discuss.list.FeedListScreen
import net.primal.android.discuss.list.FeedListViewModel
import net.primal.android.discuss.post.NewPostScreen
import net.primal.android.discuss.post.NewPostViewModel
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.explore.feed.ExploreFeedScreen
import net.primal.android.explore.feed.ExploreFeedViewModel
import net.primal.android.explore.home.ExploreHomeScreen
import net.primal.android.explore.home.ExploreHomeViewModel
import net.primal.android.explore.search.ui.SearchScreen
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.navigation.splash.SplashContract
import net.primal.android.navigation.splash.SplashScreen
import net.primal.android.navigation.splash.SplashViewModel
import net.primal.android.profile.details.ProfileScreen
import net.primal.android.profile.details.ProfileViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.thread.ThreadScreen
import net.primal.android.thread.ThreadViewModel


private fun NavController.navigateToWelcome() = navigate(
    route = "welcome",
    navOptions = navOptions { clearBackStack() }
)

private fun NavController.navigateToLogin() = navigate(route = "login")

private fun NavController.navigateToLogout() = navigate(route = "logout")

private fun NavController.navigateToFeedList() = navigate(route = "feed/list")

private fun NavController.navigateToSearch() = navigate(route = "search")

private fun NavController.navigateToNewPost(preFillContent: String?) =
    navigate(route = "feed/new?$NewPostPreFillContent=${preFillContent.orEmpty().asUrlEncoded()}")


private val NavController.topLevelNavOptions
    get() = navOptions {
        val feedDestination = backQueue.find { it.destination.route?.contains("feed") == true }
        val popUpToId = feedDestination?.destination?.id ?: 0
        popUpTo(id = popUpToId)
    }

private fun NavController.navigateToFeed(directive: String) = navigate(
    route = "feed?directive=${directive.asUrlEncoded()}",
    navOptions = navOptions { clearBackStack() },
)

private fun NavController.navigateToExplore() =
    navigate(route = "explore", navOptions = topLevelNavOptions)

private fun NavController.navigateToMessages() =
    navigate(route = "messages", navOptions = topLevelNavOptions)

private fun NavController.navigateToNotifications() =
    navigate(route = "notifications", navOptions = topLevelNavOptions)

private fun NavController.navigateToProfile(profileId: String? = null) = when {
    profileId != null -> navigate(route = "profile?$ProfileId=$profileId")
    else -> navigate(route = "profile")
}

private fun NavController.navigateToSettings() = navigate(route = "settings")

private fun NavController.navigateToThread(postId: String) = navigate(route = "thread/$postId")

private fun NavController.navigateToExploreFeed(query: String) =
    navigate(route = "explore/$query")

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
    LaunchedEffect(navController, splashViewModel) {
        splashViewModel.effect.collect {
            when (it) {
                SplashContract.SideEffect.NoActiveAccount -> navController.navigateToWelcome()
                is SplashContract.SideEffect.ActiveAccount -> navController.navigateToFeed(directive = it.userPubkey)
            }
        }
    }

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = AppTheme.shapes.medium,
    ) {
        NavHost(
            navController = navController, startDestination = "splash"
        ) {
            splash(route = "splash")

            welcome(route = "welcome", navController = navController)

            login(route = "login", navController = navController)

            logout(route = "logout", navController = navController)

            feed(
                route = "feed?$FeedDirective={$FeedDirective}",
                arguments = listOf(
                    navArgument(FeedDirective) {
                        type = NavType.StringType
                        nullable = true
                    }
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
                route = "explore/{$SearchQuery}",
                arguments = listOf(
                    navArgument(SearchQuery) {
                        type = NavType.StringType
                    }
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

            newPost(
                route = "feed/new?$NewPostPreFillContent={$NewPostPreFillContent}",
                arguments = listOf(
                    navArgument(NewPostPreFillContent) {
                        type = NavType.StringType
                        nullable = true
                    }
                ),
                navController = navController,
            )

            thread(
                route = "thread/{$PostId}",
                arguments = listOf(
                    navArgument(PostId) {
                        type = NavType.StringType
                    }
                ),
                navController = navController,
            )

            profile(
                route = "profile?$ProfileId={$ProfileId}",
                arguments = listOf(
                    navArgument(ProfileId) {
                        type = NavType.StringType
                        nullable = true
                    }
                ),
                navController = navController,
            )

            settingsNavigation(route = "settings", navController = navController)
        }
    }
}

private fun NavGraphBuilder.splash(
    route: String
) = composable(route = route) {
    SplashScreen()
}

private fun NavGraphBuilder.welcome(
    route: String,
    navController: NavController,
) = composable(route = route) {
    LockToOrientationPortrait()
    PrimalTheme(PrimalTheme.Sunset) {
        WelcomeScreen(
            onSignInClick = { navController.navigateToLogin() },
            onCreateAccountClick = { },
        )
    }
}

private fun NavGraphBuilder.login(
    route: String,
    navController: NavController,
) = composable(route = route) {
    val viewModel: LoginViewModel = hiltViewModel(it)
    PrimalTheme(PrimalTheme.Sunset) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { pubkey -> navController.navigateToFeed(pubkey) },
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
        onNewPostClick = { preFillContent -> navController.navigateToNewPost(preFillContent) },
        onPostClick = { postId -> navController.navigateToThread(postId = postId) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
    )
}

private fun NavGraphBuilder.newPost(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) {
    val viewModel = hiltViewModel<NewPostViewModel>(it)
    LockToOrientationPortrait()
    NewPostScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class)
private fun NavGraphBuilder.feedList(
    route: String,
    navController: NavController,
) = bottomSheet(
    route = route
) {
    val viewModel = hiltViewModel<FeedListViewModel>(it)
    LockToOrientationPortrait()
    FeedListScreen(
        viewModel = viewModel,
        onFeedSelected = { directive -> navController.navigateToFeed(directive = directive) }
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
        onPostClick = { postId -> navController.navigateToThread(postId)},
        onPostQuoteClick = { preFillContent -> navController.navigateToNewPost(preFillContent) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
    )
}

private fun NavGraphBuilder.search(
    route: String,
    navController: NavController,
) = composable(
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
) {
    LockToOrientationPortrait()
    DemoPrimaryScreen(
        title = stringResource(id = R.string.messages_title),
        description = "Your messages will appear here.",
        primaryDestination = PrimalTopLevelDestination.Messages,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
    )
}

private fun NavGraphBuilder.notifications(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoPrimaryScreen(
        title = stringResource(id = R.string.notifications_title),
        description = "Your notifications will appear here.",
        primaryDestination = PrimalTopLevelDestination.Notifications,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
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
        onPostQuoteClick = { preFillContent -> navController.navigateToNewPost(preFillContent) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
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
        onPostClick = { postId -> navController.navigateToThread(postId = postId) },
        onPostQuoteClick = { preFillContent -> navController.navigateToNewPost(preFillContent) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
    )
}

private fun NavGraphBuilder.logout(
    route: String,
    navController: NavController,
) = dialog(
    route = route,
) {
    val viewModel: LogoutViewModel = hiltViewModel(it)
    LockToOrientationPortrait()
    LogoutScreen(
        viewModel = viewModel,
        onClose = { navController.popBackStack() },
    )
}
