package net.primal.android.signer.provider.connect

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
import androidx.compose.ui.platform.LocalContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.android.core.compose.PrimalBottomSheetDragHandle
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.signer.SignerConnectBottomSheet
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.signer.provider.rememberAppDisplayInfo
import net.primal.android.theme.AppTheme
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.domain.nostr.cryptography.utils.assureValidPubKeyHex

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun AndroidConnectScreen(
    viewModel: AndroidConnectViewModel,
    onDismiss: () -> Unit,
    onConnectionApproved: (LocalSignerMethodResponse) -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is AndroidConnectContract.SideEffect.ConnectionSuccess -> {
                    onConnectionApproved(
                        LocalSignerMethodResponse.Success.GetPublicKey(
                            eventId = Uuid.random().toString(),
                            pubkey = it.userId.assureValidPubKeyHex(),
                        ),
                    )
                }

                is AndroidConnectContract.SideEffect.ConnectionFailure -> {
                    onConnectionApproved(
                        LocalSignerMethodResponse.Error(
                            eventId = Uuid.random().toString(),
                            message = it.error.message ?: "Unable to sign in.",
                        ),
                    )
                }
            }
        }
    }

    SnackbarErrorHandler(
        error = uiState.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { viewModel.setEvent(AndroidConnectContract.UiEvent.DismissError) },
    )

    AndroidConnectScreen(
        state = uiState,
        eventPublisher = { viewModel.setEvent(it) },
        onDismiss = onDismiss,
        snackbarHostState = snackbarHostState,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun AndroidConnectScreen(
    state: AndroidConnectContract.UiState,
    eventPublisher: (AndroidConnectContract.UiEvent) -> Unit,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appDisplayInfo = rememberAppDisplayInfo(state.appPackageName)

    ModalBottomSheet(
        sheetState = sheetState,
        contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        onDismissRequest = onDismiss,
        dragHandle = { PrimalBottomSheetDragHandle() },
    ) {
        SignerConnectBottomSheet(
            appName = appDisplayInfo?.name ?: state.appPackageName,
            appIcon = appDisplayInfo?.icon,
            appDescription = state.appPackageName,
            accounts = state.accounts,
            connecting = state.connecting,
            onConnectClick = { account, trustLevel, _ ->
                eventPublisher(
                    AndroidConnectContract.UiEvent.ConnectUser(
                        userId = account.pubkey,
                        trustLevel = trustLevel,
                        appName = appDisplayInfo?.name,
                    ),
                )
            },
            onCancelClick = onDismiss,
        )

        SnackbarHost(
            hostState = snackbarHostState,
        )
    }
}
