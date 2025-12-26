package net.primal.android.nostrconnect.approvals

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import net.primal.android.core.compose.signer.SignerEventDetails
import net.primal.android.core.compose.signer.SignerPermissionsBottomSheet
import net.primal.android.nostrconnect.approvals.PermissionsContract.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsBottomSheet(viewModel: PermissionsViewModel, content: @Composable () -> Unit) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.bottomSheetVisibility) {
        val eventDetails = remember(
            state.eventDetailsSessionEvent,
            state.parsedSignedEvent,
            state.parsedUnsignedEvent,
        ) {
            state.eventDetailsSessionEvent?.let {
                SignerEventDetails(
                    sessionEvent = it,
                    parsedSignedEvent = state.parsedSignedEvent,
                    parsedUnsignedEvent = state.parsedUnsignedEvent,
                )
            }
        }

        SignerPermissionsBottomSheet(
            sheetState = sheetState,
            events = state.sessionEvents,
            appName = state.session?.appName,
            appIconUrl = state.session?.appImageUrl,
            permissionsMap = state.permissionsMap,
            eventDetails = eventDetails,
            responding = state.responding,
            onDismissRequest = { viewModel.setEvent(UiEvent.DismissSheet) },
            onAllow = { ids, always -> viewModel.setEvent(UiEvent.Allow(ids, always)) },
            onReject = { ids, always -> viewModel.setEvent(UiEvent.Reject(ids, always)) },
            onLookUpEventDetails = { viewModel.setEvent(UiEvent.OpenEventDetails(it)) },
            onCloseEventDetails = { viewModel.setEvent(UiEvent.CloseEventDetails) },
        )
    }

    content()
}
