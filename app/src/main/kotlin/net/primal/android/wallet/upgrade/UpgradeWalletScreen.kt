package net.primal.android.wallet.upgrade

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.upgrade.UpgradeWalletContract.UiEvent
import net.primal.android.wallet.upgrade.ui.UpgradeWalletFailed
import net.primal.android.wallet.upgrade.ui.UpgradeWalletInProgress
import net.primal.android.wallet.upgrade.ui.UpgradeWalletReady
import net.primal.android.wallet.upgrade.ui.UpgradeWalletSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeWalletScreen(viewModel: UpgradeWalletViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    UpgradeWalletScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@ExperimentalMaterial3Api
@Composable
fun UpgradeWalletScreen(
    state: UpgradeWalletContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(enabled = state.status == UpgradeWalletStatus.Upgrading) {
        // Block back navigation during upgrade
    }

    PrimalScaffold(
        topBar = {
            UpgradeWalletTopAppBar(
                status = state.status,
                onClose = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = state.status,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "UpgradeWalletContent",
            ) { status ->
                when (status) {
                    UpgradeWalletStatus.Ready -> {
                        UpgradeWalletReady(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .padding(paddingValues),
                            onStartUpgrade = { eventPublisher(UiEvent.StartUpgrade) },
                        )
                    }

                    UpgradeWalletStatus.Upgrading -> {
                        UpgradeWalletInProgress(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .padding(paddingValues)
                                .padding(bottom = 80.dp),
                            currentStep = state.currentStep,
                        )
                    }

                    UpgradeWalletStatus.Success -> {
                        UpgradeWalletSuccess(
                            modifier = Modifier.fillMaxSize(),
                            onDoneClick = onClose,
                        )
                    }

                    UpgradeWalletStatus.Failed -> {
                        UpgradeWalletFailed(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .padding(paddingValues),
                            errorMessage = state.error?.message
                                ?: stringResource(id = R.string.app_generic_error),
                            onRetryClick = { eventPublisher(UiEvent.RetryUpgrade) },
                            onCloseClick = onClose,
                        )
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpgradeWalletTopAppBar(status: UpgradeWalletStatus, onClose: () -> Unit) {
    if (status != UpgradeWalletStatus.Success) {
        val showBackButton = status == UpgradeWalletStatus.Ready ||
            status == UpgradeWalletStatus.Failed
        PrimalTopAppBar(
            title = when (status) {
                UpgradeWalletStatus.Ready -> stringResource(id = R.string.wallet_upgrade_title)
                UpgradeWalletStatus.Upgrading -> stringResource(id = R.string.wallet_upgrade_upgrading_title)
                UpgradeWalletStatus.Failed -> stringResource(id = R.string.wallet_upgrade_failed_title)
                else -> ""
            },
            navigationIcon = if (showBackButton) PrimalIcons.ArrowBack else null,
            navigationIconContentDescription = if (showBackButton) {
                stringResource(id = R.string.accessibility_back_button)
            } else {
                null
            },
            showDivider = false,
            onNavigationIconClick = if (showBackButton) onClose else null,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppTheme.colorScheme.surfaceVariant,
                scrolledContainerColor = AppTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}
