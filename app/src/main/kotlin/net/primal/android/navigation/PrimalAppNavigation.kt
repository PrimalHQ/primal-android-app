package net.primal.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
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
import net.primal.android.auth.login.LoginScreen
import net.primal.android.auth.login.LoginViewModel
import net.primal.android.auth.logout.LogoutScreen
import net.primal.android.auth.logout.LogoutViewModel
import net.primal.android.auth.welcome.WelcomeScreen
import net.primal.android.core.compose.DemoPrimaryScreen
import net.primal.android.core.compose.DemoSecondaryScreen
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.feed.feed.FeedScreen
import net.primal.android.feed.feed.FeedViewModel
import net.primal.android.feed.list.FeedListScreen
import net.primal.android.feed.list.FeedListViewModel
import net.primal.android.feed.thread.ThreadScreen
import net.primal.android.feed.thread.ThreadViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import org.apache.commons.lang3.CharEncoding
import java.net.URLDecoder
import java.net.URLEncoder


const val FeedDirective = "directive"
inline val SavedStateHandle.feedDirective: String? get() = get<String>(FeedDirective)?.asUrlDecoded()

const val PostId = "postId"
inline val SavedStateHandle.postId: String
    get() = get(PostId) ?: throw IllegalArgumentException("Missing required postId argument.")

fun String.asUrlEncoded(): String = URLEncoder.encode(this, CharEncoding.UTF_8)

fun String?.asUrlDecoded() = when (this) {
    null -> null
    else -> URLDecoder.decode(this, CharEncoding.UTF_8)
}

private fun NavOptionsBuilder.clearBackStack() = popUpTo(id = 0)

private fun NavController.navigateToWelcome() = navigate(
    route = "welcome",
    navOptions = navOptions { clearBackStack() }
)

private fun NavController.navigateToLogin() = navigate(route = "login")

private fun NavController.navigateToFeedList() = navigate(route = "feed/list")

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

private fun NavController.navigateToExploreScreen() =
    navigate(route = "explore", navOptions = topLevelNavOptions)

private fun NavController.navigateToMessagesScreen() =
    navigate(route = "messages", navOptions = topLevelNavOptions)

private fun NavController.navigateToNotificationsScreen() =
    navigate(route = "notifications", navOptions = topLevelNavOptions)

private fun NavController.navigateToProfileScreen() = navigate(route = "profile")

private fun NavController.navigateToBookmarksScreen() = navigate(route = "bookmarks")

private fun NavController.navigateToUserListsScreen() = navigate(route = "userLists")

private fun NavController.navigateToSettingsScreen() = navigate(route = "settings")

private fun NavController.navigateToSignOutScreen() = navigate(route = "signOut")

private fun NavController.navigateToThreadScreen(postId: String) =
    navigate(route = "thread/$postId")

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun PrimalAppNavigation() {

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    val topLevelDestinationHandler: (PrimalTopLevelDestination) -> Unit = {
        when (it) {
            PrimalTopLevelDestination.Feed -> navController.popBackStack()
            PrimalTopLevelDestination.Explore -> navController.navigateToExploreScreen()
            PrimalTopLevelDestination.Messages -> navController.navigateToMessagesScreen()
            PrimalTopLevelDestination.Notifications -> navController.navigateToNotificationsScreen()
        }
    }

    val drawerDestinationHandler: (DrawerScreenDestination) -> Unit = {
        when (it) {
            DrawerScreenDestination.Profile -> navController.navigateToProfileScreen()
            DrawerScreenDestination.Bookmarks -> navController.navigateToBookmarksScreen()
            DrawerScreenDestination.UserLists -> navController.navigateToUserListsScreen()
            DrawerScreenDestination.Settings -> navController.navigateToSettingsScreen()
            DrawerScreenDestination.SignOut -> navController.navigateToSignOutScreen()
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

            feed(
                route = "feed?$FeedDirective={$FeedDirective}",
                arguments = listOf(navArgument(FeedDirective) {
                    type = NavType.StringType
                    nullable = true
                }),
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

            thread(
                route = "thread/{$PostId}",
                arguments = listOf(navArgument(PostId) {
                    type = NavType.StringType
                }),
                navController = navController,
            )

            profile(route = "profile", navController = navController)

            bookmarks(route = "bookmarks", navController = navController)

            userLists(route = "userLists", navController = navController)

            settings(route = "settings", navController = navController)

            signOut(route = "signOut", navController = navController)
        }
    }
}

private fun NavGraphBuilder.splash(route: String) = composable(route = route) {
    val viewModel: SplashViewModel = hiltViewModel()
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
        onPostClick = { navController.navigateToThreadScreen(postId = it) },
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
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
    FeedListScreen(viewModel = viewModel,
        onFeedSelected = { navController.navigateToFeed(directive = it) })
}

private fun NavGraphBuilder.explore(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoPrimaryScreen(
        title = "Explore",
        description = "Coming soon.",
        primaryDestination = PrimalTopLevelDestination.Explore,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
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
        title = "Messages",
        description = "Coming soon.",
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
        title = "Notifications",
        description = "Coming soon.",
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
    ThreadScreen(viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { navController.navigateToThreadScreen(it) })
}

private fun NavGraphBuilder.profile(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Profile", description = "Coming soon."
    )
}

private fun NavGraphBuilder.bookmarks(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Bookmarks", description = "Coming soon."
    )
}

private fun NavGraphBuilder.userLists(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "User Lists", description = "Coming soon."
    )
}

private fun NavGraphBuilder.settings(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Settings", description = "Coming soon."
    )
}

private fun NavGraphBuilder.signOut(
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
