@file:OptIn(ExperimentalMaterial3Api::class)

package net.primal.android.nostrconnect

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import net.primal.android.R
import net.primal.android.core.compose.signer.SignerConnectBottomSheet
import net.primal.android.core.compose.signer.model.SignerConnectCallbacks
import net.primal.android.core.compose.signer.model.SignerConnectUiState
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.android.nostrconnect.ui.NostrConnectBottomSheetDragHandle
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NostrConnectBottomSheet(viewModel: NostrConnectViewModel, onDismissRequest: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel, onDismissRequest) {
        viewModel.effects.collect {
            when (it) {
                is NostrConnectContract.SideEffect.ConnectionSuccess -> {
                    PrimalRemoteSignerService.ensureServiceStarted(context = context)
                    Toast.makeText(
                        context,
                        context.getString(R.string.nostr_connect_toast_connected),
                        Toast.LENGTH_SHORT,
                    ).show()
                    onDismissRequest()
                }
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        dragHandle = { NostrConnectBottomSheetDragHandle() },
    ) {
        SignerConnectBottomSheet(
            signerConnect = state.toSignerConnectUiState(),
            onDismissRequest = onDismissRequest,
            callbacks = SignerConnectCallbacks(
                onConnectClick = { viewModel.setEvent(NostrConnectContract.UiEvent.ClickConnect) },
                onCancelClick = onDismissRequest,
                onTabChange = { tab ->
                    viewModel.setEvent(NostrConnectContract.UiEvent.ChangeTab(tab))
                },
                onAccountSelect = { pubkey ->
                    viewModel.setEvent(NostrConnectContract.UiEvent.SelectAccount(pubkey))
                },
                onTrustLevelSelect = { level ->
                    viewModel.setEvent(NostrConnectContract.UiEvent.SelectTrustLevel(level))
                },
                onErrorDismiss = { viewModel.setEvent(NostrConnectContract.UiEvent.DismissError) },
            ),
        )
    }
}

private fun NostrConnectContract.UiState.toSignerConnectUiState(): SignerConnectUiState {
    return SignerConnectUiState(
        appName = this.appName,
        appDescription = this.appDescription,
        appImageUrl = this.appImageUrl,
        connectionUrl = this.connectionUrl,
        accounts = this.accounts,
        selectedTab = this.selectedTab,
        selectedAccount = this.selectedAccount,
        trustLevel = this.trustLevel,
        connecting = this.connecting,
        error = this.error,
    )
}
