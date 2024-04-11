package net.primal.android.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import kotlinx.serialization.encodeToString
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.wallet.activation.WalletActivationViewModel
import net.primal.android.wallet.activation.ui.WalletActivationScreen
import net.primal.android.wallet.dashboard.WalletDashboardScreen
import net.primal.android.wallet.dashboard.WalletDashboardViewModel
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.transactions.details.TransactionDetailsScreen
import net.primal.android.wallet.transactions.details.TransactionDetailsViewModel
import net.primal.android.wallet.transactions.receive.ReceivePaymentScreen
import net.primal.android.wallet.transactions.receive.ReceivePaymentViewModel
import net.primal.android.wallet.transactions.send.create.CreateTransactionScreen
import net.primal.android.wallet.transactions.send.create.CreateTransactionViewModel
import net.primal.android.wallet.transactions.send.prepare.SendPaymentScreen
import net.primal.android.wallet.transactions.send.prepare.SendPaymentViewModel
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab

private fun NavController.navigateToWalletActivation() = navigate(route = "activation")

private fun NavController.navigateToWalletSendPayment(tab: SendPaymentTab) =
    navigate(route = "send?$SEND_PAYMENT_TAB=$tab")

fun NavController.navigateToWalletCreateTransaction(draftTransaction: DraftTx) =
    navigate(
        route = "transaction/create?$DRAFT_TRANSACTION=" +
            NostrJson.encodeToString(draftTransaction).asBase64Encoded(),
    )

private fun NavController.navigateToWalletReceive() = navigate(route = "receive")

private fun NavController.navigateToTransactionDetails(txId: String) = navigate(route = "transaction/$txId")

fun NavGraphBuilder.walletNavigation(
    route: String,
    navController: NavController,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) = navigation(
    route = route,
    startDestination = "dashboard",
) {
    dashboard(
        route = "dashboard",
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        navController = navController,
    )

    activation(
        route = "activation",
        navController = navController,
    )

    send(
        route = "send?$SEND_PAYMENT_TAB={$SEND_PAYMENT_TAB}",
        arguments = listOf(
            navArgument(SEND_PAYMENT_TAB) {
                type = NavType.StringType
                nullable = true
            },
        ),
        navController = navController,
    )

    createTransaction(
        route = "transaction/create?$DRAFT_TRANSACTION={$DRAFT_TRANSACTION}",
        arguments = listOf(
            navArgument(DRAFT_TRANSACTION) {
                type = NavType.StringType
                nullable = true
            },
        ),
        navController = navController,
    )

    receive(
        route = "receive",
        navController = navController,
    )

    transactionDetails(
        route = "transaction/{$TRANSACTION_ID}",
        arguments = listOf(
            navArgument(TRANSACTION_ID) {
                type = NavType.StringType
            },
        ),
        navController = navController,
    )
}

private fun NavGraphBuilder.dashboard(
    route: String,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    navController: NavController,
) = composable(route = route) {
    val viewModel = hiltViewModel<WalletDashboardViewModel>(it)

    LockToOrientationPortrait()
    WalletDashboardScreen(
        viewModel = viewModel,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
        onWalletActivateClick = { navController.navigateToWalletActivation() },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onTransactionClick = { txId -> navController.navigateToTransactionDetails(txId) },
        onSendClick = { navController.navigateToWalletSendPayment(tab = SendPaymentTab.Nostr) },
        onScanClick = { navController.navigateToWalletSendPayment(tab = SendPaymentTab.Scan) },
        onReceiveClick = { navController.navigateToWalletReceive() },
    )
}

private fun NavGraphBuilder.activation(route: String, navController: NavController) =
    composable(route = route) {
        val viewModel = hiltViewModel<WalletActivationViewModel>(it)

        LockToOrientationPortrait()
        WalletActivationScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.send(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(route = route, arguments = arguments) {
    val viewModel = hiltViewModel<SendPaymentViewModel>(it)

    LockToOrientationPortrait()
    SendPaymentScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onCreateTransaction = { draft ->
            navController.popBackStack()
            navController.navigateToWalletCreateTransaction(draftTransaction = draft)
        },
    )
}

private fun NavGraphBuilder.createTransaction(
    route: String,
    arguments: List<NamedNavArgument>,
    navController: NavController,
) = composable(route = route, arguments = arguments) {
    val viewModel = hiltViewModel<CreateTransactionViewModel>()

    LockToOrientationPortrait()
    CreateTransactionScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.receive(route: String, navController: NavController) =
    composable(route = route) {
        val viewModel = hiltViewModel<ReceivePaymentViewModel>()

        LockToOrientationPortrait()
        ReceivePaymentScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.transactionDetails(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument>,
) = composable(route = route, arguments = arguments) {
    val viewModel = hiltViewModel<TransactionDetailsViewModel>()

    LockToOrientationPortrait()
    TransactionDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPostClick = { noteId -> navController.navigateToThread(noteId) },
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onHashtagClick = { hashtag -> navController.navigateToExploreFeed(query = hashtag) },
        onMediaClick = {
            navController.navigateToMediaGallery(
                noteId = it.noteId,
                mediaUrl = it.mediaUrl,
            )
        },
    )
}
