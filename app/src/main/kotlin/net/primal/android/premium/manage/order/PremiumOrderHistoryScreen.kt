package net.primal.android.premium.manage.order

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@Composable
fun PremiumOrderHistoryScreen(viewModel: PremiumOrderHistoryViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PremiumOrderHistoryScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumOrderHistoryScreen(
    state: PremiumOrderHistoryContract.UiState,
    eventPublisher: (PremiumOrderHistoryContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_order_history_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        bottomBar = {
            if (state.isSubscription) {
                CancelSubscriptionButton(
                    cancelling = state.cancellingSubscription,
                    onCancelConfirmed = {
                        eventPublisher(PremiumOrderHistoryContract.UiEvent.CancelSubscription)
                    },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 64.dp),
                text = "Your orders will appear here.",
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun CancelSubscriptionButton(cancelling: Boolean, onCancelConfirmed: () -> Unit) {
    var showCancelSubscriptionDialog by remember { mutableStateOf(false) }
    if (showCancelSubscriptionDialog) {
        CancelSubscriptionAlertDialog(
            onDismissRequest = { showCancelSubscriptionDialog = false },
            onConfirm = {
                showCancelSubscriptionDialog = false
                onCancelConfirmed()
            },
        )
    }

    PrimalLoadingButton(
        loading = cancelling,
        enabled = !cancelling,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        text = "Cancel Premium Subscription",
    )
}

@Composable
private fun CancelSubscriptionAlertDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Cancel Premium?",
            )
        },
        text = {
            Text(
                text = "Your subscription will remain active until the end of your current billing period, " +
                    "so you can continue enjoying premium features until then. " +
                    "No further charges will be made after that date.",
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "Keep Premium",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Cancel Subscription",
                )
            }
        },
    )
}
