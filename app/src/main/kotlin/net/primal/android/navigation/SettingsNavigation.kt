package net.primal.android.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.settings.appearance.AppearanceSettingsScreen
import net.primal.android.settings.appearance.AppearanceSettingsViewModel
import net.primal.android.settings.feeds.FeedsSettingsScreen
import net.primal.android.settings.feeds.FeedsSettingsViewModel
import net.primal.android.settings.home.PrimalSettingsSection
import net.primal.android.settings.home.SettingsHomeScreen
import net.primal.android.settings.home.SettingsHomeViewModel
import net.primal.android.settings.keys.AccountSettingsScreen
import net.primal.android.settings.keys.AccountSettingsViewModel
import net.primal.android.settings.muted.list.MutedSettingsScreen
import net.primal.android.settings.muted.list.MutedSettingsViewModel
import net.primal.android.settings.notifications.NotificationsSettingsScreen
import net.primal.android.settings.notifications.NotificationsSettingsViewModel
import net.primal.android.settings.wallet.WalletScreen
import net.primal.android.settings.wallet.WalletViewModel
import net.primal.android.settings.zaps.ZapSettingsScreen
import net.primal.android.settings.zaps.ZapSettingsViewModel

private fun NavController.navigateToAccount() = navigate(route = "account_settings")
private fun NavController.navigateToWallet() = navigate(route = "wallet_settings")
private fun NavController.navigateToAppearance() = navigate(route = "appearance_settings")
fun NavController.navigateToNotificationsSettings() = navigate(route = "notifications_settings")
private fun NavController.navigateToFeeds() = navigate(route = "feeds_settings")
private fun NavController.navigateToZaps() = navigate(route = "zaps_settings")
private fun NavController.navigateToMutedAccounts() = navigate(route = "muted_accounts_settings")

fun NavGraphBuilder.settingsNavigation(route: String, navController: NavController) =
    navigation(
        route = route,
        startDestination = "home_settings",
    ) {
        home(
            route = "home_settings",
            onClose = { navController.navigateUp() },
            onSettingsSectionClick = {
                when (it) {
                    PrimalSettingsSection.Account -> navController.navigateToAccount()
                    PrimalSettingsSection.Wallet -> navController.navigateToWallet()
                    PrimalSettingsSection.Appearance -> navController.navigateToAppearance()
                    PrimalSettingsSection.Notifications -> navController.navigateToNotificationsSettings()
                    PrimalSettingsSection.Feeds -> navController.navigateToFeeds()
                    PrimalSettingsSection.Zaps -> navController.navigateToZaps()
                    PrimalSettingsSection.MutedAccounts -> navController.navigateToMutedAccounts()
                }
            },
        )

        keys(route = "account_settings", navController = navController)
        wallet(
            route = "wallet_settings?nwcUrl={$NWC_URL}",
            arguments = listOf(
                navArgument(NWC_URL) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
            navController = navController,
        )
        appearance(route = "appearance_settings", navController = navController)
        mutedAccounts(route = "muted_accounts_settings", navController = navController)
        notifications(route = "notifications_settings", navController = navController)
        feeds(route = "feeds_settings", navController = navController)
        zaps(route = "zaps_settings", navController = navController)
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

private fun NavGraphBuilder.keys(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<AccountSettingsViewModel>(it)
        LockToOrientationPortrait()
        AccountSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.wallet(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(
    route = route,
    arguments = arguments,
) { navBackEntry ->
    val viewModel = hiltViewModel<WalletViewModel>(navBackEntry)
    LockToOrientationPortrait()
    WalletScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.notifications(route: String, navController: NavController) =
    composable(
        route = route,
    ) { navBackEntry ->
        val viewModel = hiltViewModel<NotificationsSettingsViewModel>(navBackEntry)
        LockToOrientationPortrait()
        NotificationsSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.appearance(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<AppearanceSettingsViewModel>()
        LockToOrientationPortrait()
        AppearanceSettingsScreen(viewModel = viewModel, onClose = { navController.navigateUp() })
    }

private fun NavGraphBuilder.feeds(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<FeedsSettingsViewModel>()
        LockToOrientationPortrait()
        FeedsSettingsScreen(viewModel = viewModel, onClose = { navController.navigateUp() })
    }

private fun NavGraphBuilder.zaps(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<ZapSettingsViewModel>(it)
        LockToOrientationPortrait()
        ZapSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.mutedAccounts(route: String, navController: NavController) =
    composable(
        route = route,
    ) {
        val viewModel = hiltViewModel<MutedSettingsViewModel>(it)
        LockToOrientationPortrait()
        MutedSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        )
    }
