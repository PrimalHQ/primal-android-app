package net.primal.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.primal.android.feed.FeedViewModel
import net.primal.android.feed.ui.HomeScreen
import net.primal.android.login.LoginViewModel
import net.primal.android.login.ui.LoginScreen

@Composable
fun PrimalNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "feed"
    ) {

        login(
            route = "login",
            navController = navController,
        )

        feed(
            route = "feed",
            navController = navController,
        )

    }
}

private fun NavGraphBuilder.login(
    route: String,
    navController: NavController,
) = composable(route = route) {
    val viewModel = hiltViewModel<LoginViewModel>()

    LoginScreen(
        viewModel = viewModel
    )
}

private fun NavGraphBuilder.feed(
    route: String,
    navController: NavController,
) = composable(route = route) {
    val viewModel = hiltViewModel<FeedViewModel>()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect {
            when (it) {
                else -> {
                    navController.navigate(route = "login")
                }
            }
        }
    }


    HomeScreen(
        viewModel = viewModel
    )
}