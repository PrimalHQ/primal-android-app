@file:OptIn(ExperimentalMaterial3Api::class)

package net.primal.android.nostrconnect.connect

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import net.primal.android.R
import net.primal.android.core.compose.PrimalBottomSheetDragHandle
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.signer.SignerConnectBottomSheet
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.service.PrimalNwcService
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NostrConnectBottomSheet(viewModel: NostrConnectViewModel, onDismissRequest: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { viewModel.setEvent(NostrConnectContract.UiEvent.DismissError) },
    )

    LaunchedEffect(viewModel, onDismissRequest) {
        viewModel.effects.collect {
            when (it) {
                is NostrConnectContract.SideEffect.ConnectionSuccess -> {
                    PrimalRemoteSignerService.ensureServiceStarted(context = context)
                    if (it.requiresNwcService) {
                        PrimalNwcService.start(context = context, userId = it.userId)
                    }
                    if (it.callbackUri != null) {
                        onDismissRequest()
                        uriHandler.openUriSafely(it.callbackUri)
                    } else {
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
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        dragHandle = { PrimalBottomSheetDragHandle() },
    ) {
        Box {
            SignerConnectBottomSheet(
                appName = state.appName,
                appDescription = state.appDescription,
                appImageUrl = state.appImageUrl,
                accounts = state.accounts,
                connecting = state.connecting,
                onConnectClick = { account, trustLevel, dailyBudget ->
                    viewModel.setEvent(
                        NostrConnectContract.UiEvent.ConnectUser(
                            userId = account.pubkey,
                            trustLevel = trustLevel,
                            dailyBudget = dailyBudget,
                        ),
                    )
                },
                onCancelClick = onDismissRequest,
                hasNwcRequest = state.hasNwcRequest,
                budgetToUsdMap = state.budgetToUsdMap,
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
