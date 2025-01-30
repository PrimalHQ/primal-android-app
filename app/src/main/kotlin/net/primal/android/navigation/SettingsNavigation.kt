package net.primal.android.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import net.primal.android.LocalPrimalTheme
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.settings.appearance.AppearanceSettingsScreen
import net.primal.android.settings.appearance.di.appearanceSettingsViewModel
import net.primal.android.settings.content.ContentDisplaySettingsScreen
import net.primal.android.settings.content.ContentDisplaySettingsViewModel
import net.primal.android.settings.home.PrimalSettingsSection
import net.primal.android.settings.home.SettingsHomeScreen
import net.primal.android.settings.home.SettingsHomeViewModel
import net.primal.android.settings.keys.KeysSettingsScreen
import net.primal.android.settings.keys.KeysSettingsViewModel
import net.primal.android.settings.muted.list.MutedSettingsScreen
import net.primal.android.settings.muted.list.MutedSettingsViewModel
import net.primal.android.settings.network.NetworkSettingsScreen
import net.primal.android.settings.network.NetworkSettingsViewModel
import net.primal.android.settings.notifications.NotificationsSettingsScreen
import net.primal.android.settings.notifications.NotificationsSettingsViewModel
import net.primal.android.settings.wallet.WalletSettingsScreen
import net.primal.android.settings.wallet.WalletSettingsViewModel
import net.primal.android.settings.wallet.connection.NwcNewWalletConnectionScreen
import net.primal.android.settings.wallet.connection.NwcNewWalletConnectionViewModel
import net.primal.android.settings.wallet.link.LinkPrimalWalletScreen
import net.primal.android.settings.wallet.link.LinkPrimalWalletViewModel
import net.primal.android.settings.wallet.nwc.NwcQrCodeScannerScreen
import net.primal.android.settings.wallet.nwc.NwcQrCodeScannerViewModel
import net.primal.android.settings.zaps.ZapSettingsScreen
import net.primal.android.settings.zaps.ZapSettingsViewModel

private fun NavController.navigateToAccountSettings() = navigate(route = "account_settings")
private fun NavController.navigateToNetworkSettings() = navigate(route = "network")
private fun NavController.navigateToWalletSettings() = navigate(route = "wallet_settings")
private fun NavController.navigateToWalletScanNwcUrl() = navigate(route = "wallet_settings/scanNwcUrl")

// private fun NavController.navigateToNewWalletConnection() = navigate(route = "wallet_settings/newWalletConnection")
private fun NavController.navigateToAppearanceSettings() = navigate(route = "appearance_settings")
private fun NavController.navigateToContentDisplaySettings() = navigate(route = "content_display")
fun NavController.navigateToNotificationsSettings() = navigate(route = "notifications_settings")
private fun NavController.navigateToZapsSettings() = navigate(route = "zaps_settings")
private fun NavController.navigateToMutedAccounts() = navigate(route = "muted_accounts_settings")
private fun NavController.navigateToLinkPrimalWallet(
    appName: String? = null,
    appIcon: String? = null,
    callback: String,
) {
    navigate(
        route = "wallet_settings/linkPrimalWallet?appName=$appName&appIcon=$appIcon&callback=$callback",
    )
}

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
                    PrimalSettingsSection.Keys -> navController.navigateToAccountSettings()
                    PrimalSettingsSection.Network -> navController.navigateToNetworkSettings()
                    PrimalSettingsSection.Wallet -> navController.navigateToWalletSettings()
                    PrimalSettingsSection.Appearance -> navController.navigateToAppearanceSettings()
                    PrimalSettingsSection.ContentDisplay -> navController.navigateToContentDisplaySettings()
                    PrimalSettingsSection.Notifications -> navController.navigateToNotificationsSettings()
                    PrimalSettingsSection.Zaps -> navController.navigateToZapsSettings()
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
        linkPrimalWallet(
            route = "wallet_settings/linkPrimalWallet?appName={$NWC_APP_NAME}" +
                "&appIcon={$NWC_APP_ICON}&callback={$NWC_CALLBACK}",
            arguments = listOf(
                navArgument(NWC_APP_NAME) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(NWC_APP_ICON) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(NWC_CALLBACK) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )
        scanNwcUrl(route = "wallet_settings/scanNwcUrl", navController = navController)
        newWalletConnection(route = "wallet_settings/newWalletConnection", navController = navController)
        network(route = "network", navController = navController)
        appearance(route = "appearance_settings", navController = navController)
        contentDisplay(route = "content_display", navController = navController)
        mutedAccounts(route = "muted_accounts_settings", navController = navController)
        notifications(route = "notifications_settings", navController = navController)
        zaps(route = "zaps_settings", navController = navController)
    }

private fun NavGraphBuilder.home(
    route: String,
    onClose: () -> Unit,
    onSettingsSectionClick: (PrimalSettingsSection) -> Unit,
) = composable(
    route = route,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
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
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<KeysSettingsViewModel>(it)
        LockToOrientationPortrait()
        KeysSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.network(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<NetworkSettingsViewModel>(it)
        LockToOrientationPortrait()
        NetworkSettingsScreen(
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
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val viewModel = hiltViewModel<WalletSettingsViewModel>(navBackEntry)
    LockToOrientationPortrait()
    WalletSettingsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onEditProfileClick = { navController.navigateToProfileEditor() },
        onOtherConnectClick = { navController.navigateToWalletScanNwcUrl() },
        onCreateNewWalletConnection = {
//            navController.navigateToNewWalletConnection()
            navController.navigateToLinkPrimalWallet(
                callback = "callback",
            )
        },
    )
}

private fun NavGraphBuilder.scanNwcUrl(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<NwcQrCodeScannerViewModel>()
        LockToOrientationPortrait()
        NwcQrCodeScannerScreen(
            viewModel = viewModel,
            onClose = { navController.popBackStack() },
        )
    }

private fun NavGraphBuilder.linkPrimalWallet(
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
    val viewModel = hiltViewModel<LinkPrimalWalletViewModel>()
    LockToOrientationPortrait()
    LinkPrimalWalletScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.newWalletConnection(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<NwcNewWalletConnectionViewModel>()
        LockToOrientationPortrait()
        NwcNewWalletConnectionScreen(
            viewModel = viewModel,
            onClose = { navController.popBackStack() },
        )
    }

private fun NavGraphBuilder.notifications(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
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
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = appearanceSettingsViewModel(primalTheme = LocalPrimalTheme.current)
        LockToOrientationPortrait()
        AppearanceSettingsScreen(viewModel = viewModel, onClose = { navController.navigateUp() })
    }

private fun NavGraphBuilder.contentDisplay(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<ContentDisplaySettingsViewModel>(it)
        LockToOrientationPortrait()
        ContentDisplaySettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.zaps(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
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
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<MutedSettingsViewModel>(it)
        LockToOrientationPortrait()
        MutedSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        )
    }
