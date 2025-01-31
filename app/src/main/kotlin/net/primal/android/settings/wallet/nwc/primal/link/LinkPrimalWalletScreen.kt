package net.primal.android.settings.wallet.nwc.primal.link

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.DailyBudgetPicker
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.ext.openUriSafely
import net.primal.android.settings.wallet.nwc.primal.PrimalNwcDefaults
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletContract.SideEffect
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletContract.UiEvent
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletContract.UiState
import net.primal.android.settings.wallet.nwc.primal.ui.DailyBudgetBottomSheet
import net.primal.android.settings.wallet.nwc.primal.ui.WalletConnectionEditorHeader
import net.primal.android.settings.wallet.nwc.primal.ui.WalletConnectionFooter
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun LinkPrimalWalletScreen(viewModel: LinkPrimalWalletViewModel, onDismiss: () -> Unit) {
    val state = viewModel.state.collectAsState()

    val uriHandler = LocalUriHandler.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                is SideEffect.UriReceived -> {
                    onDismiss()
                    uriHandler.openUriSafely(it.callbackUri)
                }
            }
        }
    }

    LinkPrimalWalletScreen(
        eventPublisher = { viewModel.setEvent(it) },
        state = state.value,
        onClose = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkPrimalWalletScreen(
    eventPublisher: (UiEvent) -> Unit,
    state: UiState,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_wallet_link_primal_wallet_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            WalletConnectionEditor(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
                state = state,
                eventPublisher = eventPublisher,
            )
        },
        bottomBar = {
            WalletConnectionFooter(
                loading = state.creatingSecret,
                enabled = !state.creatingSecret,
                primaryButtonText = stringResource(
                    id = R.string.settings_wallet_link_give_wallet_access_button,
                ),
                onPrimaryButtonClick = {
                    eventPublisher(UiEvent.CreateWalletConnection)
                },
                secondaryButtonText = stringResource(
                    id = R.string.settings_wallet_new_nwc_connection_cancel_button,
                ),
                onSecondaryButtonClick = onClose,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletConnectionEditor(
    modifier: Modifier,
    eventPublisher: (UiEvent) -> Unit,
    state: UiState,
) {
    var showDailyBudgetBottomSheet by rememberSaveable { mutableStateOf(false) }

    if (showDailyBudgetBottomSheet) {
        DailyBudgetBottomSheet(
            initialDailyBudget = state.dailyBudget,
            onDismissRequest = { showDailyBudgetBottomSheet = false },
            onBudgetSelected = { dailyBudget ->
                eventPublisher(UiEvent.DailyBudgetChanged(dailyBudget))
            },
            budgetOptions = PrimalNwcDefaults.ALL_BUDGET_OPTIONS,
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletConnectionEditorHeader(
            appName = state.appName,
            appIcon = state.appIcon,
        )

        Column {
            Text(
                modifier = Modifier.padding(horizontal = 34.dp),
                text = stringResource(
                    id = R.string.settings_wallet_link_external_app_request,
                    state.appName ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
                ),
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(21.dp))

            DailyBudgetPicker(
                dailyBudget = state.dailyBudget,
                onChangeDailyBudgetBottomSheetVisibility = { showDailyBudgetBottomSheet = it },
            )

            Spacer(modifier = Modifier.height(21.dp))

            Text(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = stringResource(id = R.string.settings_wallet_new_nwc_connection_hint),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 23.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

class LinkPrimalWalletUiStateProvider : PreviewParameterProvider<UiState> {
    override val values: Sequence<UiState>
        get() = sequenceOf(
            UiState(),
            UiState(creatingSecret = true),
        )
}

@Preview
@Composable
private fun PreviewCreateNewWalletConnectionScreen(
    @PreviewParameter(LinkPrimalWalletUiStateProvider::class)
    state: UiState,
) {
    PrimalPreview(
        primalTheme = PrimalTheme.Sunset,
    ) {
        LinkPrimalWalletScreen(
            state = state,
            eventPublisher = {},
            onClose = {},
        )
    }
}
