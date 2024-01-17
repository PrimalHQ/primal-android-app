package net.primal.android.wallet.transactions.send.prepare

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalCircleButton
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.transactions.send.create.DraftTransaction
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTabNostr
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTabScan
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTabText

@Composable
fun SendPaymentScreen(
    viewModel: SendPaymentViewModel,
    onClose: () -> Unit,
    onCreateTransaction: (DraftTransaction) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect {
            when (it) {
                is SendPaymentContract.SideEffect.DraftTransactionReady -> {
                    onCreateTransaction(it.draft)
                }
            }
        }
    }

    SendPaymentScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPaymentScreen(
    state: SendPaymentContract.UiState,
    eventPublisher: (SendPaymentContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    var activeTab by remember { mutableStateOf(state.initialTab) }
    var closingScreen by remember { mutableStateOf(false) }
    val keyboardVisible by keyboardVisibilityAsState()

    BackHandler {
        closingScreen = true
        onClose()
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = if (activeTab != SendPaymentTab.Scan) state.error else null,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is SendPaymentContract.UiState.SendPaymentError.LightningAddressNotFound ->
                    context.getString(
                        R.string.wallet_send_payment_error_nostr_user_without_lightning_address,
                        it.userDisplayName ?: context.getString(
                            R.string.wallet_send_payment_nostr_user_generic,
                        ),
                    )

                is SendPaymentContract.UiState.SendPaymentError.ParseException ->
                    context.getString(
                        R.string.wallet_send_payment_error_unable_to_parse_text,
                    )
            }
        },
        onErrorDismiss = { eventPublisher(SendPaymentContract.UiEvent.DismissError) },
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = activeTab.labelResId),
                navigationIcon = PrimalIcons.ArrowBack,
                showDivider = activeTab != SendPaymentTab.Nostr,
                onNavigationIconClick = {
                    closingScreen = true
                    onClose()
                },
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues),
            ) {
                when (activeTab) {
                    SendPaymentTab.Nostr -> {
                        SendPaymentTabNostr(
                            onProfileClick = {
                                eventPublisher(SendPaymentContract.UiEvent.ProcessProfileData(profileId = it))
                            },
                        )
                    }

                    SendPaymentTab.Scan -> {
                        SendPaymentTabScan(
                            isClosing = closingScreen,
                            onQrCodeDetected = {
                                eventPublisher(SendPaymentContract.UiEvent.ProcessTextData(text = it.value))
                            },
                        )
                    }

                    SendPaymentTab.Text -> {
                        SendPaymentTabText(
                            parsing = state.parsing,
                            onTextConfirmed = {
                                eventPublisher(SendPaymentContract.UiEvent.ProcessTextData(text = it))
                            },
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column {
                val hideHeight by animateDpAsState(targetValue = 0.dp, label = "HideSendPaymentTabsBar")
                val showHeight by animateDpAsState(targetValue = 96.dp, label = "ShowSendPaymentTabsBar")
                PrimalDivider()
                SendPaymentTabsBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (keyboardVisible) hideHeight else showHeight)
                        .background(color = AppTheme.colorScheme.surface),
                    activeTab = activeTab,
                    onTabClick = {
                        if (activeTab == it) {
                            // Nothing
                        } else {
                            activeTab = it
                        }
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
fun SendPaymentTabsBar(
    modifier: Modifier = Modifier,
    activeTab: SendPaymentTab,
    onTabClick: (SendPaymentTab) -> Unit,
) {
    Row(
        modifier = modifier.navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SendPaymentTab.entries.forEach {
            TabButton(
                icon = it.icon,
                selected = activeTab == it,
                onClick = { onTabClick(it) },
            )
        }
    }
}

@Composable
private fun TabButton(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    PrimalCircleButton(
        modifier = Modifier.size(56.dp),
        containerColor = if (selected) AppTheme.colorScheme.onSurface else AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = if (selected) AppTheme.colorScheme.surface else AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
        )
    }
}
