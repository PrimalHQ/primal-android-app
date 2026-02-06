package net.primal.android.navigation

import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.settings.account.AccountSettingsScreen
import net.primal.android.settings.account.AccountSettingsViewModel
import net.primal.android.settings.appearance.AppearanceSettingsScreen
import net.primal.android.settings.appearance.di.appearanceSettingsViewModel
import net.primal.android.settings.connected.ConnectedAppsScreen
import net.primal.android.settings.connected.ConnectedAppsViewModel
import net.primal.android.settings.connected.details.local.LocalAppDetailsScreen
import net.primal.android.settings.connected.details.local.LocalAppDetailsViewModel
import net.primal.android.settings.connected.details.remote.RemoteAppDetailsScreen
import net.primal.android.settings.connected.details.remote.RemoteAppDetailsViewModel
import net.primal.android.settings.connected.event.local.LocalEventDetailsScreen
import net.primal.android.settings.connected.event.local.LocalEventDetailsViewModel
import net.primal.android.settings.connected.event.remote.RemoteEventDetailsScreen
import net.primal.android.settings.connected.event.remote.RemoteEventDetailsViewModel
import net.primal.android.settings.connected.permissions.local.LocalAppPermissionsScreen
import net.primal.android.settings.connected.permissions.local.LocalAppPermissionsViewModel
import net.primal.android.settings.connected.permissions.remote.RemoteAppPermissionsScreen
import net.primal.android.settings.connected.permissions.remote.RemoteAppPermissionsViewModel
import net.primal.android.settings.connected.session.local.LocalSessionDetailsScreen
import net.primal.android.settings.connected.session.local.LocalSessionDetailsViewModel
import net.primal.android.settings.connected.session.remote.RemoteSessionDetailsScreen
import net.primal.android.settings.connected.session.remote.RemoteSessionDetailsViewModel
import net.primal.android.settings.content.ContentDisplaySettingsScreen
import net.primal.android.settings.content.ContentDisplaySettingsViewModel
import net.primal.android.settings.developer.DeveloperToolsScreen
import net.primal.android.settings.developer.DeveloperToolsViewModel
import net.primal.android.settings.home.PrimalSettingsSection
import net.primal.android.settings.home.SettingsHomeScreen
import net.primal.android.settings.home.SettingsHomeViewModel
import net.primal.android.settings.media.MediaUploadsSettingsScreen
import net.primal.android.settings.media.MediaUploadsSettingsViewModel
import net.primal.android.settings.muted.MutedSettingsScreen
import net.primal.android.settings.muted.MutedSettingsViewModel
import net.primal.android.settings.network.NetworkSettingsScreen
import net.primal.android.settings.network.NetworkSettingsViewModel
import net.primal.android.settings.notifications.NotificationsSettingsScreen
import net.primal.android.settings.notifications.NotificationsSettingsViewModel
import net.primal.android.settings.wallet.domain.parseAsPrimalWalletNwc
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionScreen
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionViewModel
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletScreen
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletViewModel
import net.primal.android.settings.wallet.nwc.scan.NwcQrCodeScannerScreen
import net.primal.android.settings.wallet.nwc.scan.NwcQrCodeScannerViewModel
import net.primal.android.settings.wallet.settings.WalletSettingsScreen
import net.primal.android.settings.wallet.settings.WalletSettingsViewModel
import net.primal.android.settings.zaps.ZapSettingsScreen
import net.primal.android.settings.zaps.ZapSettingsViewModel
import net.primal.android.wallet.restore.RestoreWalletScreen
import net.primal.android.wallet.restore.RestoreWalletViewModel

private fun NavController.navigateToAccountSettings() = navigate(route = "account_settings")
private fun NavController.navigateToNetworkSettings() = navigate(route = "network")
fun NavController.navigateToWalletSettings() = navigate(route = "wallet_settings")
private fun NavController.navigateToWalletScanNwcUrl() = navigate(route = "wallet_settings/scan_nwc_url")

private fun NavController.navigateToCreateNewWalletConnection() = navigate(route = "wallet_settings/create_new_nwc")
private fun NavController.navigateToAppearanceSettings() = navigate(route = "appearance_settings")
private fun NavController.navigateToContentDisplaySettings() = navigate(route = "content_display")
fun NavController.navigateToNotificationsSettings() = navigate(route = "notifications_settings")
private fun NavController.navigateToZapsSettings() = navigate(route = "zaps_settings")
private fun NavController.navigateToMutedAccounts() = navigate(route = "muted_accounts_settings")
private fun NavController.navigateToMediaUploads() = navigate(route = "media_uploads_settings")
fun NavController.navigateToConnectedApps() = navigate(route = "connected_apps")
private fun NavController.navigateToDeveloperTools() = navigate(route = "developer_tools")

private fun NavController.navigateToRemoteSessionDetails(sessionId: String) =
    navigate(route = "session_details/remote/$sessionId")

private fun NavController.navigateToLocalSessionDetails(sessionId: String) =
    navigate(route = "session_details/local/$sessionId")

private fun NavController.navigateToRemoteEventDetails(eventId: String) =
    navigate(route = "event_details/remote/$eventId")

private fun NavController.navigateToLocalEventDetails(eventId: String) =
    navigate(route = "event_details/local/$eventId")

fun NavController.navigateToConnectedAppDetails(clientPubKey: String) =
    navigate(route = "connected_apps/remote/$clientPubKey")

fun NavController.navigateToLocalAppDetails(identifier: String) = navigate(route = "connected_apps/local/$identifier")

private fun NavController.navigateToRemoteAppPermissions(clientPubKey: String) =
    navigate(route = "connected_apps/remote/$clientPubKey/permissions")

private fun NavController.navigateToLocalAppPermissions(identifier: String) =
    navigate(route = "connected_apps/local/$identifier/permissions")

private fun NavController.navigateToWalletRestore() = navigate(route = "wallet_settings/restore")

fun NavController.navigateToLinkPrimalWallet(
    appName: String? = null,
    appIcon: String? = null,
    callback: String,
) = navigate(
    route = "wallet_settings/link_primal_wallet?appName=$appName&appIcon=$appIcon&callback=$callback",
)

@Suppress("LongMethod")
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
                    PrimalSettingsSection.Account -> navController.navigateToAccountSettings()
                    PrimalSettingsSection.Network -> navController.navigateToNetworkSettings()
                    PrimalSettingsSection.Wallet -> navController.navigateToWalletSettings()
                    PrimalSettingsSection.Appearance -> navController.navigateToAppearanceSettings()
                    PrimalSettingsSection.ContentDisplay -> navController.navigateToContentDisplaySettings()
                    PrimalSettingsSection.Notifications -> navController.navigateToNotificationsSettings()
                    PrimalSettingsSection.Zaps -> navController.navigateToZapsSettings()
                    PrimalSettingsSection.MutedAccounts -> navController.navigateToMutedAccounts()
                    PrimalSettingsSection.MediaUploads -> navController.navigateToMediaUploads()
                    PrimalSettingsSection.ConnectedApps -> navController.navigateToConnectedApps()
                }
            },
            onDeveloperToolsClick = { navController.navigateToDeveloperTools() },
        )

        account(route = "account_settings", navController = navController)
        wallet(
            route = "wallet_settings",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "nostr+walletconnect://.*"
                },
                navDeepLink {
                    uriPattern = "nostrwalletconnect://.*"
                },
            ),
            navController = navController,
        )
        walletRestore(route = "wallet_settings/restore", navController = navController)
        linkPrimalWallet(
            route = "wallet_settings/link_primal_wallet",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "nostrnwc://.*"
                },
                navDeepLink {
                    uriPattern = "nostrnwc+primal://.*"
                },
            ),
            navController = navController,
        )
        scanNwcUrl(route = "wallet_settings/scan_nwc_url", navController = navController)
        createNewWalletConnection(route = "wallet_settings/create_new_nwc", navController = navController)
        network(route = "network", navController = navController)
        appearance(route = "appearance_settings", navController = navController)
        contentDisplay(route = "content_display", navController = navController)
        mutedAccounts(route = "muted_accounts_settings", navController = navController)
        mediaUploads(route = "media_uploads_settings", navController = navController)
        notifications(route = "notifications_settings", navController = navController)
        zaps(route = "zaps_settings", navController = navController)
        connectedApps(route = "connected_apps", navController = navController)
        developerTools(route = "developer_tools", navController = navController)
        connectedRemoteAppDetails(
            route = "connected_apps/remote/{$REMOTE_LOGIN_CLIENT_PUBKEY}",
            arguments = listOf(
                navArgument(REMOTE_LOGIN_CLIENT_PUBKEY) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "primal://signer/{$REMOTE_LOGIN_CLIENT_PUBKEY}"
                },
            ),
        )
        connectedLocalAppDetails(
            route = "connected_apps/local/{$IDENTIFIER}",
            arguments = listOf(
                navArgument(IDENTIFIER) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )
        connectedRemoteAppPermissions(
            route = "connected_apps/remote/{$REMOTE_LOGIN_CLIENT_PUBKEY}/permissions",
            arguments = listOf(
                navArgument(REMOTE_LOGIN_CLIENT_PUBKEY) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )
        connectedLocalAppPermissions(
            route = "connected_apps/local/{$IDENTIFIER}/permissions",
            arguments = listOf(
                navArgument(IDENTIFIER) {
                    type = NavType.StringType
                },
            ),
            navController = navController,
        )
        remoteSessionDetails(
            route = "session_details/remote/{$SESSION_ID}",
            navController = navController,
            arguments = listOf(
                navArgument(SESSION_ID) { type = NavType.StringType },
            ),
        )
        localSessionDetails(
            route = "session_details/local/{$SESSION_ID}",
            navController = navController,
            arguments = listOf(
                navArgument(SESSION_ID) { type = NavType.StringType },
            ),
        )
        remoteEventDetails(
            route = "event_details/remote/{$EVENT_ID}",
            navController = navController,
            arguments = listOf(
                navArgument(EVENT_ID) { type = NavType.StringType },
            ),
        )
        localEventDetails(
            route = "event_details/local/{$EVENT_ID}",
            navController = navController,
            arguments = listOf(
                navArgument(EVENT_ID) { type = NavType.StringType },
            ),
        )
    }

private fun NavGraphBuilder.home(
    route: String,
    onClose: () -> Unit,
    onSettingsSectionClick: (PrimalSettingsSection) -> Unit,
    onDeveloperToolsClick: () -> Unit,
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
        onDeveloperToolsClick = onDeveloperToolsClick,
    )
}

private fun NavGraphBuilder.account(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<AccountSettingsViewModel>(it)
        LockToOrientationPortrait()
        AccountSettingsScreen(
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
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) { navBackEntry ->
    val nwcUrl = LocalActivity.current?.intent?.data?.toString()
    val viewModel = hiltViewModel<WalletSettingsViewModel, WalletSettingsViewModel.Factory> { factory ->
        factory.create(nwcConnectionUrl = nwcUrl)
    }
    LockToOrientationPortrait()
    WalletSettingsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onEditProfileClick = { navController.navigateToProfileEditor() },
        onScanNwcClick = { navController.navigateToWalletScanNwcUrl() },
        onCreateNewWalletConnection = { navController.navigateToCreateNewWalletConnection() },
        onRestoreWalletClick = { navController.navigateToWalletRestore() },
        onBackupWalletClick = { walletId -> navController.navigateToWalletBackup(walletId) },
    )
}

private fun NavGraphBuilder.walletRestore(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<RestoreWalletViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        RestoreWalletScreen(
            viewModel = viewModel,
            onClose = { navController.popBackStack() },
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
    deepLinks: List<NavDeepLink>,
    navController: NavController,
) = composable(
    route = route,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val activity = LocalActivity.current
    fun dismissLinkPrimalWallet() {
        if (!navController.popBackStack()) {
            activity?.finishAfterTransition()
        }
    }

    val nwcPrimalUrl = activity?.intent?.data?.toString()
    if (nwcPrimalUrl == null) {
        dismissLinkPrimalWallet()
        return@composable
    }

    val viewModel = hiltViewModel<LinkPrimalWalletViewModel, LinkPrimalWalletViewModel.Factory> { factory ->
        factory.create(nwcRequest = nwcPrimalUrl.parseAsPrimalWalletNwc())
    }
    LockToOrientationPortrait()
    LinkPrimalWalletScreen(
        viewModel = viewModel,
        onDismiss = { dismissLinkPrimalWallet() },
    )
}

private fun NavGraphBuilder.createNewWalletConnection(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<CreateNewWalletConnectionViewModel>()
        LockToOrientationPortrait()
        CreateNewWalletConnectionScreen(
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
            noteCallbacks = noteCallbacksHandler(navController),
            onClose = { navController.navigateUp() },
            onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
            onGoToWallet = { navController.navigateToWallet() },
        )
    }

private fun NavGraphBuilder.mediaUploads(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<MediaUploadsSettingsViewModel>(it)
        LockToOrientationPortrait()
        MediaUploadsSettingsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.connectedApps(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<ConnectedAppsViewModel>()
        LockToOrientationPortrait()
        ConnectedAppsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onConnectedAppClick = { clientPubKey -> navController.navigateToConnectedAppDetails(clientPubKey) },
            onLocalAppClick = { identifier -> navController.navigateToLocalAppDetails(identifier) },
        )
    }

private fun NavGraphBuilder.developerTools(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<DeveloperToolsViewModel>()
        LockToOrientationPortrait()
        DeveloperToolsScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.connectedLocalAppDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<LocalAppDetailsViewModel>()
    LockToOrientationPortrait()
    LocalAppDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onSessionClick = { sessionId ->
            navController.navigateToLocalSessionDetails(sessionId)
        },
        onPermissionDetailsClick = { identifier ->
            navController.navigateToLocalAppPermissions(identifier)
        },
    )
}

private fun NavGraphBuilder.connectedRemoteAppDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
    deepLinks: List<NavDeepLink>,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<RemoteAppDetailsViewModel>()
    LockToOrientationPortrait()
    RemoteAppDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onSessionClick = { sessionId ->
            navController.navigateToRemoteSessionDetails(sessionId)
        },
        onPermissionDetailsClick = { clientPubKey ->
            navController.navigateToRemoteAppPermissions(clientPubKey)
        },
    )
}

private fun NavGraphBuilder.connectedRemoteAppPermissions(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<RemoteAppPermissionsViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    RemoteAppPermissionsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.connectedLocalAppPermissions(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<LocalAppPermissionsViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    LocalAppPermissionsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.remoteSessionDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<RemoteSessionDetailsViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    RemoteSessionDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onEventClick = { eventId ->
            navController.navigateToRemoteEventDetails(eventId = eventId)
        },
    )
}

private fun NavGraphBuilder.localSessionDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<LocalSessionDetailsViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    LocalSessionDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onEventClick = { eventId ->
            navController.navigateToLocalEventDetails(eventId = eventId)
        },
    )
}

private fun NavGraphBuilder.remoteEventDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<RemoteEventDetailsViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    RemoteEventDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.localEventDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<LocalEventDetailsViewModel>()
    ApplyEdgeToEdge()
    LockToOrientationPortrait()
    LocalEventDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}
