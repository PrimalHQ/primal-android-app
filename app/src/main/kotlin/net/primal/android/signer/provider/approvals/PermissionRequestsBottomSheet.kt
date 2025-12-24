package net.primal.android.signer.provider.approvals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.primal.android.core.compose.getListItemShape
import net.primal.android.nostrconnect.permissions.AppRequestListItem
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiEvent
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiState
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.LocalSignerMethodResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestsBottomSheet(
    viewModel: PermissionRequestsViewModel,
    onDismiss: () -> Unit,
    onCompleted: (List<LocalSignerMethodResponse>) -> Unit,
) {
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is PermissionRequestsContract.SideEffect.ApprovalSuccess -> {
                    onCompleted(it.approvedMethods)
                }
            }
        }
    }

    val uiState = viewModel.state.collectAsState()
    PermissionRequestsBottomSheet(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onDismiss = onDismiss,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun PermissionRequestsBottomSheet(
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        onDismissRequest = onDismiss,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                onClick = {
                    eventPublisher(UiEvent.ApproveSelectedMethods)
                },
            ) {
                Text(text = "Approve all")
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    items = state.requestQueue,
                    key = { _, item -> item.eventId },
                ) { index, event ->
                    AppRequestListItem(
                        shape = getListItemShape(index = index, listSize = state.requestQueue.size),
                        event = event,
                        permissionsMap = emptyMap(),
                        isSelected = false,
                        onSelectClick = { },
                        onDeselectClick = { },
                        showDivider = index < state.requestQueue.lastIndex,
                        onDetailsClick = { },
                    )
                }
            }
        }
    }
}
