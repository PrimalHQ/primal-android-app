package net.primal.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import net.primal.android.navigation.splash.SplashContract
import net.primal.android.navigation.splash.SplashScreen
import net.primal.android.navigation.splash.SplashViewModel
import net.primal.android.profile.details.ProfileScreen
import net.primal.android.profile.details.ProfileViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme


private fun NavController.navigateToWelcome() = navigate(
    route = "welcome",
    navOptions = navOptions { clearBackStack() }
)

private fun NavController.navigateToLogin() = navigate(route = "login")

private fun NavController.navigateToLogout() = navigate(route = "logout")

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

private fun NavController.navigateToExplore() =
    navigate(route = "explore", navOptions = topLevelNavOptions)

private fun NavController.navigateToMessages() =
    navigate(route = "messages", navOptions = topLevelNavOptions)

private fun NavController.navigateToNotifications() =
    navigate(route = "notifications", navOptions = topLevelNavOptions)

private fun NavController.navigateToProfile(profileId: String? = null) = when {
    profileId != null -> navigate(route = "profile?profileId=$profileId")
    else -> navigate(route = "profile")
}

private fun NavController.navigateToBookmarks() = navigate(route = "bookmarks")

private fun NavController.navigateToUserLists() = navigate(route = "userLists")

private fun NavController.navigateToSettings() = navigate(route = "settings")

private fun NavController.navigateToThread(postId: String) = navigate(route = "thread/$postId")

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
            DrawerScreenDestination.Bookmarks -> navController.navigateToBookmarks()
            DrawerScreenDestination.UserLists -> navController.navigateToUserLists()
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

            bookmarks(route = "bookmarks", navController = navController)

            userLists(route = "userLists", navController = navController)

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
        onPostClick = { postId -> navController.navigateToThread(postId = postId) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
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
    ThreadScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { postId -> navController.navigateToThread(postId) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
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
        onProfileClick = { profileId -> navController.navigateToProfile(profileId = profileId) },
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
        title = "Bookmarks",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
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
        title = "User Lists",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
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
