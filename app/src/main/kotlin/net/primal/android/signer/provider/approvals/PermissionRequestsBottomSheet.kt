package net.primal.android.signer.provider.approvals

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import net.primal.android.core.compose.signer.SignerEventDetails
import net.primal.android.core.compose.signer.SignerPermissionsBottomSheet
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiEvent
import net.primal.android.signer.provider.rememberAppDisplayInfo
import net.primal.domain.account.model.LocalSignerMethodResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestsBottomSheet(
    viewModel: PermissionRequestsViewModel,
    onDismiss: () -> Unit,
    onCompleted: (List<LocalSignerMethodResponse>) -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is PermissionRequestsContract.SideEffect.ApprovalSuccess -> {
                    onCompleted(it.approvedMethods)
                }
                is PermissionRequestsContract.SideEffect.RejectionSuccess -> {
                    onCompleted(emptyList())
                }
            }
        }
    }

    val packageName = uiState.callingPackage
    val appDisplayInfo = if (packageName != null) {
        rememberAppDisplayInfo(packageName)
    } else {
        null
    }

    val eventDetails = remember(
        uiState.eventDetailsSessionEvent,
        uiState.parsedSignedEvent,
        uiState.parsedUnsignedEvent,
    ) {
        uiState.eventDetailsSessionEvent?.let {
            SignerEventDetails(
                sessionEvent = it,
                parsedSignedEvent = uiState.parsedSignedEvent,
                parsedUnsignedEvent = uiState.parsedUnsignedEvent,
            )
        }
    }

    SignerPermissionsBottomSheet(
        sheetState = sheetState,
        events = uiState.requestQueue,
        appName = appDisplayInfo?.name,
        appIcon = appDisplayInfo?.icon,
        permissionsMap = uiState.permissionsMap,
        eventDetails = eventDetails,
        responding = uiState.responding,
        onDismissRequest = onDismiss,
        onAllow = { ids, always -> viewModel.setEvent(UiEvent.Allow(ids, always)) },
        onReject = { ids, always -> viewModel.setEvent(UiEvent.Reject(ids, always)) },
        onLookUpEventDetails = { viewModel.setEvent(UiEvent.OpenEventDetails(it)) },
        onCloseEventDetails = { viewModel.setEvent(UiEvent.CloseEventDetails) },
    )
}
