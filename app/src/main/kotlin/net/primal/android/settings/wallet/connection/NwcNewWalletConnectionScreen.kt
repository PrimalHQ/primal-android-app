package net.primal.android.settings.wallet.connection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalOutlinedTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppConnection
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppForeground
import net.primal.android.theme.AppTheme

private val IconBackgroundColor = Color(0xFFE5E5E5)

@Composable
fun NwcNewWalletConnectionScreen(viewModel: NwcNewWalletConnectionViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    NwcNewWalletConnectionScreen(
        eventPublisher = { viewModel.setEvent(it) },
        state = state.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NwcNewWalletConnectionScreen(
    eventPublisher: (NwcNewWalletConnectionContract.UiEvent) -> Unit,
    state: NwcNewWalletConnectionContract.UiState,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_new_wallet_connection_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = state.secret,
                transitionSpec = { slideInVertically() togetherWith slideOutVertically() },
            ) { targetSecretState ->
                if (targetSecretState == null) {
                    WalletConnectionEditor(
                        scrollState = scrollState,
                        paddingValues = paddingValues,
                        state = state,
                        eventPublisher = eventPublisher,
                        onClose = onClose,
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletConnectionEditor(
    scrollState: ScrollState,
    paddingValues: PaddingValues,
    state: NwcNewWalletConnectionContract.UiState,
    eventPublisher: (NwcNewWalletConnectionContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    var showDailyBudgetBottomSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(color = AppTheme.colorScheme.surfaceVariant)
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletConnectionHeader()

        Column {
            PrimalOutlinedTextField(
                header = stringResource(id = R.string.settings_new_wallet_app_name_input_header),
                value = state.appName,
                onValueChange = {
                    eventPublisher(NwcNewWalletConnectionContract.UiEvent.AppNameChanged(it))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .clickable { showDailyBudgetBottomSheet = true }
                    .clip(RoundedCornerShape(12.dp)),
                colors = ListItemDefaults.colors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
                headlineContent = {
                    Text(text = stringResource(id = R.string.settings_wallet_header_daily_budget))
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.dailyBudget?.isNotBlank() == true) {
                            Text(
                                text = state.dailyBudget.toLong().let {
                                    "%,d ${stringResource(id = R.string.wallet_sats_suffix)}".format(it)
                                },
                                style = AppTheme.typography.bodyMedium,
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.settings_wallet_no_limit),
                                style = AppTheme.typography.bodyMedium,
                            )
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    }
                },
            )

            if (showDailyBudgetBottomSheet) {
                NwcDailyBudgetBottomSheet(
                    initialDailyBudget = state.dailyBudget,
                    onDismissRequest = { showDailyBudgetBottomSheet = false },
                    onBudgetSelected = { dailyBudget ->
                        eventPublisher(NwcNewWalletConnectionContract.UiEvent.DailyBudgetChanged(dailyBudget))
                    },
                )
            }

            Spacer(modifier = Modifier.height(21.dp))

            Text(
                modifier = Modifier.padding(horizontal = 21.dp),
                text = stringResource(id = R.string.settings_new_wallet_app_revoke_label),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }

        CreateNewConnectionButton(
            creatingSecret = state.creatingSecret,
            onCreateNewConnection = {
                eventPublisher(NwcNewWalletConnectionContract.UiEvent.CreateWalletConnection)
            },
            onClose = onClose,
        )
    }
}

@Composable
private fun CreateNewConnectionButton(
    creatingSecret: Boolean,
    onCreateNewConnection: () -> Unit,
    onClose: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            PrimalLoadingButton(
                text = stringResource(id = R.string.settings_new_wallet_create_new_connection_button),
                enabled = !creatingSecret,
                loading = creatingSecret,
                onClick = {
                    keyboardController?.hide()
                    onCreateNewConnection()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
            )
        }

        Text(
            modifier = Modifier.clickable { onClose() },
            text = stringResource(id = R.string.settings_new_wallet_app_cancel),
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(7.dp))
    }
}

@Composable
private fun WalletConnectionHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(top = 50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.primal_nwc_logo),
                contentDescription = "Primal Wallet",
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .padding(17.dp)
                    .size(54.dp),
                tint = Color.Unspecified,
            )

            Text(modifier = Modifier.padding(top = 13.dp), text = "Primal Wallet")
        }

        Icon(
            modifier = Modifier.offset(y = (-13).dp),
            imageVector = PrimalIcons.NwcExternalAppConnection,
            contentDescription = "Connection",
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(IconBackgroundColor)
                    .padding(21.dp)
                    .size(54.dp),
                imageVector = PrimalIcons.NwcExternalAppForeground,
                contentDescription = "External App",
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )

            Text(modifier = Modifier.padding(top = 13.dp), text = "External App")
        }
    }
}
