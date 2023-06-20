package net.primal.android.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import net.primal.android.core.compose.DemoPrimaryScreen
import net.primal.android.core.compose.DemoSecondaryScreen
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.feed.feed.FeedScreen
import net.primal.android.feed.feed.FeedViewModel
import net.primal.android.feed.list.FeedListScreen
import net.primal.android.feed.list.FeedListViewModel
import net.primal.android.feed.thread.ThreadScreen
import net.primal.android.feed.thread.ThreadViewModel
import net.primal.android.login.LoginViewModel
import net.primal.android.login.ui.DemoLoginScreen
import net.primal.android.theme.AppTheme
import org.apache.commons.lang3.CharEncoding
import java.net.URLDecoder
import java.net.URLEncoder


const val FeedDirective = "directive"
inline val SavedStateHandle.feedDirective: String? get() = get<String>(FeedDirective)?.asUrlDecoded()

const val PostId = "postId"
inline val SavedStateHandle.postId: String get() = get(PostId)
    ?: throw IllegalArgumentException("Missing required postId argument.")

fun String.asUrlEncoded(): String = URLEncoder.encode(this, CharEncoding.UTF_8)

fun String?.asUrlDecoded() = when (this) {
    null -> null
    else -> URLDecoder.decode(this, CharEncoding.UTF_8)
}

private fun NavOptionsBuilder.clearBackStack() = popUpTo(id = 0)

private fun NavController.navigateToFeedList() = navigate(route = "feed/list")

private val NavController.topLevelNavOptions
    get() = navOptions {
        val feedDestination = backQueue.find { it.destination.route?.contains("feed") == true }
        val popUpToId = feedDestination?.destination?.id ?: 0
        popUpTo(id = popUpToId)
    }

private fun NavController.navigateToFeed(directive: String) =
    navigate(
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

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = AppTheme.shapes.medium,
    ) {
        NavHost(
            navController = navController,
            startDestination = "demo"
        ) {

            demoLogin(
                route = "demo",
                navController = navController,
            )

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
                route = "profile",
                navController = navController
            )

            bookmarks(
                route = "bookmarks",
                navController = navController,
            )

            userLists(
                route = "userLists",
                navController = navController,
            )

            settings(
                route = "settings",
                navController = navController,
            )

            signOut(
                route = "signOut",
                navController = navController,
            )
        }
    }

}

private fun NavGraphBuilder.demoLogin(
    route: String,
    navController: NavController,
) = composable(route = route) {
    // Default settings are fetched in LoginViewModel for demo
    hiltViewModel<LoginViewModel>()

    DemoLoginScreen(
        onFeedSelected = {
            navController.navigateToFeed(it)
        }
    )
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
    FeedListScreen(
        viewModel = viewModel,
        onFeedSelected = { navController.navigateToFeed(directive = it) }
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
    ThreadScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { navController.navigateToThreadScreen(it) }
    )
}

private fun NavGraphBuilder.profile(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    DemoSecondaryScreen(
        title = "Profile",
        description = "Coming soon."
    )
}

private fun NavGraphBuilder.bookmarks(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    DemoSecondaryScreen(
        title = "Bookmarks",
        description = "Coming soon."
    )
}

private fun NavGraphBuilder.userLists(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    DemoSecondaryScreen(
        title = "User Lists",
        description = "Coming soon."
    )
}

private fun NavGraphBuilder.settings(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    DemoSecondaryScreen(
        title = "Settings",
        description = "Coming soon."
    )
}

private fun NavGraphBuilder.signOut(
    route: String,
    navController: NavController,
) = dialog(
    route = route,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = { navController.popBackStack() },
        title = {
            Text(
                text = "Sign out",
                style = AppTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Do you really want to sign out?",
                style = AppTheme.typography.bodyLarge
            )
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "Sign out"
                )
            }
        },
    )
}
