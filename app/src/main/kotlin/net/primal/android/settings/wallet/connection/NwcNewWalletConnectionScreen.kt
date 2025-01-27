package net.primal.android.settings.wallet.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalOutlinedTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

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
                            eventPublisher(NwcNewWalletConnectionContract.UiEvent.AppNameChangedEvent(it))
                        },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimalOutlinedTextField(
                        header = "Daily budget:",
                        value = state.appName,
                        onValueChange = {
                            eventPublisher(NwcNewWalletConnectionContract.UiEvent.AppNameChangedEvent(it))
                        },
                    )
                }

                CreateNewConnectionButton(
                    loading = state.loading,
                    onCreateNewConnection = {
                        eventPublisher(NwcNewWalletConnectionContract.UiEvent.CreateWalletConnection)
                    },
                    onClose = onClose,
                )
            }
        },
    )
}

@Composable
private fun CreateNewConnectionButton(
    loading: Boolean,
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
                .height(86.dp)
                .padding(horizontal = 32.dp),
        ) {
            PrimalLoadingButton(
                text = stringResource(id = R.string.settings_new_wallet_create_new_connection_button),
                enabled = !loading,
                loading = loading,
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
            text = "Cancel",
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
                painter = painterResource(id = R.drawable.primal_wave_logo_summer),
                contentDescription = "Primal Wallet",
                modifier = Modifier.size(64.dp),
            )
            Text(modifier = Modifier.padding(top = 11.dp), text = "Primal Wallet")
        }

        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = "Connection",
            modifier = Modifier.size(42.dp),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.primal_wave_logo_winter),
                contentDescription = "External App",
                modifier = Modifier.size(64.dp),
            )
            Text(modifier = Modifier.padding(top = 11.dp), text = "External App")
        }
    }
}
