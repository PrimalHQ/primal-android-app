package net.primal.android.settings.wallet.link

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.R
import net.primal.android.core.compose.DailyBudgetPicker
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppConnection
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppForeground
import net.primal.android.notes.feed.note.ui.attachment.NoteImageLoadingPlaceholder
import net.primal.android.settings.wallet.connection.DailyBudgetBottomSheet
import net.primal.android.settings.wallet.connection.NewWalletConnectionFooter
import net.primal.android.theme.AppTheme

@Composable
fun LinkPrimalWalletScreen(viewModel: LinkPrimalWalletViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    LinkPrimalWalletScreen(
        eventPublisher = { viewModel.setEvent(it) },
        state = state.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkPrimalWalletScreen(
    eventPublisher: (LinkPrimalWalletContract.UiEvent) -> Unit,
    state: LinkPrimalWalletContract.UiState,
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
            AnimatedContent(targetState = state.secret) { secret ->
                when (secret) {
                    null -> {
                        WalletConnectionEditor(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .verticalScroll(rememberScrollState())
                                .padding(paddingValues),
                            state = state,
                            eventPublisher = eventPublisher,
                        )
                    }
                }
            }
        },
        bottomBar = {
            when (state.secret) {
                null -> NewWalletConnectionFooter(
                    loading = state.creatingSecret,
                    enabled = !state.creatingSecret,
                    primaryButtonText = stringResource(
                        id = R.string.settings_wallet_link_give_wallet_access,
                    ),
                    onPrimaryButtonClick = onClose,
                    secondaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_cancel_button,
                    ),
                    onSecondaryButtonClick = onClose,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletConnectionEditor(
    modifier: Modifier,
    eventPublisher: (LinkPrimalWalletContract.UiEvent) -> Unit,
    state: LinkPrimalWalletContract.UiState,
) {
    var showDailyBudgetBottomSheet by rememberSaveable { mutableStateOf(false) }

    if (showDailyBudgetBottomSheet) {
        DailyBudgetBottomSheet(
            initialDailyBudget = state.dailyBudget,
            onDismissRequest = { showDailyBudgetBottomSheet = false },
            onBudgetSelected = { dailyBudget ->
                eventPublisher(LinkPrimalWalletContract.UiEvent.DailyBudgetChanged(dailyBudget))
            },
            budgetOptions = LinkPrimalWalletContract.budgetOptions,
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
                modifier = Modifier.padding(horizontal = 21.dp),
                text = stringResource(
                    id = R.string.settings_wallet_link_external_app_request,
                    state.appName ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
                ),
                style = AppTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(21.dp))

            DailyBudgetPicker(
                dailyBudget = state.dailyBudget,
                onChangeDailyBudgetBottomSheetVisibility = { showDailyBudgetBottomSheet = it },
            )

            Spacer(modifier = Modifier.height(21.dp))

            Text(
                modifier = Modifier.padding(horizontal = 21.dp),
                text = stringResource(id = R.string.settings_wallet_new_nwc_connection_hint),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WalletConnectionEditorHeader(
    modifier: Modifier = Modifier,
    appName: String?,
    appIcon: String?,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.primal_nwc_logo),
                contentDescription = stringResource(id = R.string.settings_wallet_nwc_primal_wallet),
                modifier = Modifier
                    .clip(AppTheme.shapes.small)
                    .size(99.dp),
                tint = Color.Unspecified,
            )

            Text(
                modifier = Modifier.padding(top = 13.dp),
                text = stringResource(id = R.string.settings_wallet_nwc_primal_wallet),
            )
        }

        Icon(
            modifier = Modifier.offset(y = (-13).dp),
            imageVector = PrimalIcons.NwcExternalAppConnection,
            contentDescription = "Connection",
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (appIcon.isNullOrEmpty()) {
                Icon(
                    modifier = Modifier
                        .clip(AppTheme.shapes.small)
                        .background(color = Color(color = 0xFFE5E5E5))
                        .padding(21.dp)
                        .size(54.dp),
                    imageVector = PrimalIcons.NwcExternalAppForeground,
                    contentDescription = appName ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            } else {
                SubcomposeAsyncImage(
                    model = appIcon,
                    modifier = Modifier
                        .size(99.dp),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    loading = { NoteImageLoadingPlaceholder() },
                    error = {
                        Icon(
                            modifier = Modifier
                                .clip(AppTheme.shapes.small)
                                .background(color = Color(color = 0xFFE5E5E5))
                                .padding(21.dp)
                                .size(54.dp),
                            imageVector = PrimalIcons.NwcExternalAppForeground,
                            contentDescription = appName
                                ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
                            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        )
                    },
                )
            }

            Text(
                modifier = Modifier.padding(top = 13.dp),
                text = appName ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
            )
        }
    }
}
