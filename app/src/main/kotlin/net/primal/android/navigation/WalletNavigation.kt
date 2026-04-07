package net.primal.android.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.LockToOrientationPortrait
import net.primal.android.scan.ScanCodeContract.ScanMode
import net.primal.android.wallet.backup.WalletBackupContract
import net.primal.android.wallet.backup.WalletBackupScreen
import net.primal.android.wallet.backup.WalletBackupViewModel
import net.primal.android.wallet.faq.WalletUpgradeFaqScreen
import net.primal.android.wallet.picker.WalletPickerBottomSheet
import net.primal.android.wallet.picker.WalletPickerViewModel
import net.primal.android.wallet.transactions.details.TransactionDetailsScreen
import net.primal.android.wallet.transactions.details.TransactionDetailsViewModel
import net.primal.android.wallet.transactions.receive.ReceivePaymentScreen
import net.primal.android.wallet.transactions.receive.ReceivePaymentViewModel
import net.primal.android.wallet.transactions.send.create.CreateTransactionScreen
import net.primal.android.wallet.transactions.send.create.CreateTransactionViewModel
import net.primal.android.wallet.transactions.send.prepare.SendPaymentScreen
import net.primal.android.wallet.transactions.send.prepare.SendPaymentViewModel
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab
import net.primal.android.wallet.upgrade.UpgradeWalletScreen
import net.primal.android.wallet.upgrade.UpgradeWalletViewModel
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.wallet.DraftTx

fun NavController.navigateToWalletBackup(walletId: String) = navigate(route = "walletBackup/$walletId")

internal fun NavController.navigateToWalletSendPayment(tab: SendPaymentTab) =
    navigate(route = "walletSend?$SEND_PAYMENT_TAB=$tab")

fun NavController.navigateToWalletCreateTransaction(draftTransaction: DraftTx? = null, lnbc: String? = null) {
    require(draftTransaction != null || lnbc != null)
    val tx = draftTransaction ?: DraftTx(lnInvoice = lnbc)
    navigate(
        route = "walletTransaction/create" +
            "?$DRAFT_TRANSACTION=${tx.encodeToJsonString().asBase64Encoded()}" +
            if (lnbc != null) {
                "&lnbc=$lnbc"
            } else {
                ""
            },
    )
}

internal fun NavController.navigateToWalletReceive() = navigate(route = "walletReceive")

fun NavController.navigateToWalletUpgrade() = navigate(route = "walletUpgrade")

fun NavController.navigateToWalletUpgradeFaq() = navigate(route = "walletUpgradeFaq")

internal fun NavController.navigateToTransactionDetails(txId: String) = navigate(route = "walletTransaction/$txId")

fun NavController.navigateToWalletPicker() = navigate(route = "walletPicker")

fun NavGraphBuilder.walletScreens(navController: NavController) {
    walletPicker(navController = navController)

    backup(
        route = "walletBackup/{$WALLET_ID}",
        arguments = listOf(
            navArgument(WALLET_ID) {
                type = NavType.StringType
            },
        ),
        navController = navController,
    )

    send(
        route = "walletSend?$SEND_PAYMENT_TAB={$SEND_PAYMENT_TAB}",
        arguments = listOf(
            navArgument(SEND_PAYMENT_TAB) {
                type = NavType.StringType
                nullable = true
            },
        ),
        navController = navController,
    )

    createTransaction(
        route = "walletTransaction/create?$DRAFT_TRANSACTION={$DRAFT_TRANSACTION}&$LNBC={$LNBC}",
        arguments = listOf(
            navArgument(DRAFT_TRANSACTION) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(LNBC) {
                type = NavType.StringType
                nullable = true
            },
        ),
        navController = navController,
    )

    receive(
        route = "walletReceive",
        navController = navController,
    )

    upgradeWallet(
        route = "walletUpgrade",
        navController = navController,
    )

    walletUpgradeFaq(
        route = "walletUpgradeFaq",
        navController = navController,
    )

    transactionDetails(
        route = "walletTransaction/{$TRANSACTION_ID}",
        arguments = listOf(
            navArgument(TRANSACTION_ID) {
                type = NavType.StringType
            },
        ),
        navController = navController,
    )
}

private fun NavGraphBuilder.backup(
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
    val viewModel = hiltViewModel<WalletBackupViewModel>(it)

    ApplyEdgeToEdge()
    LockToOrientationPortrait()

    WalletBackupScreen(
        viewModel = viewModel,
        callbacks = WalletBackupContract.ScreenCallbacks(
            onBackupComplete = { navController.popBackStack() },
            onClose = { navController.navigateUp() },
        ),
    )
}

private fun NavGraphBuilder.send(
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
    val viewModel = hiltViewModel<SendPaymentViewModel>(it)

    LockToOrientationPortrait()
    SendPaymentScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        onPromoCodeScan = { promoCode ->
            navController.popBackStack()
            navController.navigateToScanCode(ScanMode.Anything, promoCode)
        },
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
) = composable(
    route = route,
    arguments = arguments,
    enterTransition = { primalSlideInHorizontallyFromEnd },
    exitTransition = { primalScaleOut },
    popEnterTransition = { primalScaleIn },
    popExitTransition = { primalSlideOutHorizontallyToEnd },
) {
    val viewModel = hiltViewModel<CreateTransactionViewModel>()

    LockToOrientationPortrait()
    CreateTransactionScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
    )
}

private fun NavGraphBuilder.receive(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<ReceivePaymentViewModel>()

        LockToOrientationPortrait()
        ReceivePaymentScreen(
            viewModel = viewModel,
            onBuyPremium = { navController.navigateToPremiumBuying() },
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.upgradeWallet(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        val viewModel = hiltViewModel<UpgradeWalletViewModel>()

        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        UpgradeWalletScreen(
            viewModel = viewModel,
            onClose = { navController.navigateUp() },
            onFaqClick = { navController.navigateToWalletUpgradeFaq() },
        )
    }

private fun NavGraphBuilder.walletUpgradeFaq(route: String, navController: NavController) =
    composable(
        route = route,
        enterTransition = { primalSlideInHorizontallyFromEnd },
        exitTransition = { primalScaleOut },
        popEnterTransition = { primalScaleIn },
        popExitTransition = { primalSlideOutHorizontallyToEnd },
    ) {
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        WalletUpgradeFaqScreen(
            onClose = { navController.navigateUp() },
        )
    }

private fun NavGraphBuilder.transactionDetails(
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
    val viewModel = hiltViewModel<TransactionDetailsViewModel>()

    LockToOrientationPortrait()
    TransactionDetailsScreen(
        viewModel = viewModel,
        onClose = { navController.navigateUp() },
        noteCallbacks = noteCallbacksHandler(navController),
    )
}

private fun NavGraphBuilder.walletPicker(navController: NavController) =
    dialog(
        route = "walletPicker",
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val viewModel = hiltViewModel<WalletPickerViewModel>()
        ApplyEdgeToEdge()
        LockToOrientationPortrait()
        WalletPickerBottomSheet(
            viewModel = viewModel,
            onDismissRequest = { navController.popBackStack() },
        )
    }
