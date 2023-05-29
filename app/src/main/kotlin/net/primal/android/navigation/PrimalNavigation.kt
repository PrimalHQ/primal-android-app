package net.primal.android.navigation

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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import net.primal.android.feed.FeedViewModel
import net.primal.android.feed.list.FeedListScreen
import net.primal.android.feed.list.FeedListViewModel
import net.primal.android.feed.ui.FeedScreen
import net.primal.android.login.LoginViewModel
import net.primal.android.login.ui.DemoLoginScreen
import net.primal.android.theme.PrimalTheme
import org.apache.commons.lang3.CharEncoding
import java.net.URLDecoder
import java.net.URLEncoder


const val FeedDirective = "directive"
inline val SavedStateHandle.feedDirective: String? get() = get<String>(FeedDirective)?.asUrlDecoded()

private fun NavOptionsBuilder.clearBackStack() = popUpTo(id = 0)
private fun NavController.navigateToFeedList() = navigate(route = "feed/list")
private fun NavController.navigateToFeed(directive: String) = navigate(
    route = "feed?directive=${directive.asUrlEncoded()}",
    navOptions = navOptions { clearBackStack() }
)

fun String.asUrlEncoded(): String = URLEncoder.encode(this, CharEncoding.UTF_8)

fun String?.asUrlDecoded() = when (this) {
    null -> null
    else -> URLDecoder.decode(this, CharEncoding.UTF_8)
}


@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun PrimalNavigation() {

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = PrimalTheme.shapes.medium,
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
            )

            feedList(
                route = "feed/list",
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
) = composable(
    route = route,
    arguments = arguments,
) {
    val viewModel = hiltViewModel<FeedViewModel>(it)

    FeedScreen(
        viewModel = viewModel,
        onFeedsClick = { navController.navigateToFeedList() }
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class)
private fun NavGraphBuilder.feedList(
    route: String,
    navController: NavController,
) = bottomSheet(
    route = route
) {
    val viewModel = hiltViewModel<FeedListViewModel>()
    FeedListScreen(
        viewModel = viewModel,
        onFeedSelected = { navController.navigateToFeed(directive = it) }
    )
}
