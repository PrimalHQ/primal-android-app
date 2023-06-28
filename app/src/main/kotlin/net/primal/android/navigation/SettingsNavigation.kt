package net.primal.android.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import net.primal.android.core.compose.DemoSecondaryScreen
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.settings.home.PrimalSettingsSection
import net.primal.android.settings.home.SettingsHomeScreen
import net.primal.android.settings.home.SettingsHomeViewModel
import net.primal.android.settings.keys.KeysScreen
import net.primal.android.settings.keys.KeysViewModel


private fun NavController.navigateToKeys() = navigate(route = "keys")
private fun NavController.navigateToWallet() = navigate(route = "wallet")
private fun NavController.navigateToAppearance() = navigate(route = "appearance")
private fun NavController.navigateToNotifications() = navigate(route = "notifications")
private fun NavController.navigateToNetwork() = navigate(route = "network")
private fun NavController.navigateToFeeds() = navigate(route = "feeds")
private fun NavController.navigateToZaps() = navigate(route = "zaps")

fun NavGraphBuilder.settingsNavigation(
    route: String,
    navController: NavController,
) = navigation(
    route = route,
    startDestination = "home"
) {
    home(
        route = "home",
        onClose = { navController.navigateUp() },
        onSettingsSectionClick = {
            when (it) {
                PrimalSettingsSection.Keys -> navController.navigateToKeys()
                PrimalSettingsSection.Wallet -> navController.navigateToWallet()
                PrimalSettingsSection.Appearance -> navController.navigateToAppearance()
                PrimalSettingsSection.Notifications -> navController.navigateToNotifications()
                PrimalSettingsSection.Network -> navController.navigateToNetwork()
                PrimalSettingsSection.Feeds -> navController.navigateToFeeds()
                PrimalSettingsSection.Zaps -> navController.navigateToZaps()
            }
        }
    )

    keys(route = "keys", navController = navController)
    wallet(route = "wallet", navController = navController)
    appearance(route = "appearance", navController = navController)
    notifications(route = "notifications", navController = navController)
    network(route = "network", navController = navController)
    feeds(route = "feeds", navController = navController)
    zaps(route = "zaps", navController = navController)
}

private fun NavGraphBuilder.home(
    route: String,
    onClose: () -> Unit,
    onSettingsSectionClick: (PrimalSettingsSection) -> Unit,
) = composable(
    route = route,
) {
    val viewModel = hiltViewModel<SettingsHomeViewModel>(it)
    LockToOrientationPortrait()
    SettingsHomeScreen(
        viewModel = viewModel,
        onClose = onClose,
        onSettingsSectionClick = onSettingsSectionClick,
    )
}

private fun NavGraphBuilder.keys(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {
    val viewModel = hiltViewModel<KeysViewModel>(it)
    LockToOrientationPortrait()
    KeysScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() }
    )
}

private fun NavGraphBuilder.wallet(route: String, navController: NavController) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Wallet",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.appearance(route: String, navController: NavController) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Appearance",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.notifications(route: String, navController: NavController) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Notifications",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.network(route: String, navController: NavController) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Network",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.feeds(route: String, navController: NavController) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Feeds",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.zaps(route: String, navController: NavController) = composable(
    route = route,
) {
    LockToOrientationPortrait()
    DemoSecondaryScreen(
        title = "Zaps",
        description = "Coming soon.",
        onClose = { navController.navigateUp() },
    )
}
