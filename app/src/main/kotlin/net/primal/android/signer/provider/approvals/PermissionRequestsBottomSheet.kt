package net.primal.android.signer.provider.approvals

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
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
import net.primal.data.account.signer.local.LocalSignerMethodResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestsBottomSheet(
    viewModel: PermissionRequestsViewModel,
    onCompleted: (result: RequestsResults) -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is PermissionRequestsContract.SideEffect.RequestsCompleted -> {
                    onCompleted(RequestsResults(approved = it.approved, rejected = it.rejected))
                }
            }
        }
    }

    val appDisplayInfo = rememberAppDisplayInfo(uiState.callingPackage)

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
        appName = appDisplayInfo.name,
        appIcon = appDisplayInfo.icon,
        permissionsMap = uiState.permissionsMap,
        eventDetails = eventDetails,
        responding = uiState.responding,
        onDismissRequest = { viewModel.setEvent(UiEvent.RejectAll) },
        onAllow = { ids, always -> viewModel.setEvent(UiEvent.Allow(ids, always)) },
        onReject = { ids, always -> viewModel.setEvent(UiEvent.Reject(ids, always)) },
        onLookUpEventDetails = { viewModel.setEvent(UiEvent.OpenEventDetails(it)) },
        onCloseEventDetails = { viewModel.setEvent(UiEvent.CloseEventDetails) },
    )
}

data class RequestsResults(
    val approved: List<LocalSignerMethodResponse> = emptyList(),
    val rejected: List<LocalSignerMethodResponse> = emptyList(),
)
