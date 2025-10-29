package net.primal.android.nostrconnect.list

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.nostrconnect.list.ActiveSessionsContract.UiState
import net.primal.android.nostrconnect.selectableItem
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionsBottomSheet(
    viewModel: ActiveSessionsViewModel,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { viewModel.setEvent(ActiveSessionsContract.UiEvent.DismissError) },
    )

    LaunchedEffect(viewModel, onDismissRequest) {
        viewModel.effect.collect {
            when (it) {
                is ActiveSessionsContract.SideEffect.SessionsDisconnected -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.nostr_connect_sessions_disconnected_toast),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        ActiveNwcSessionsContent(
            state = state,
            eventPublisher = { viewModel.setEvent(it) },
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun ActiveNwcSessionsContent(
    state: UiState,
    eventPublisher: (ActiveSessionsContract.UiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderSection(
                allSelected = state.allSessionsSelected,
                onSelectAllClick = { eventPublisher(ActiveSessionsContract.UiEvent.SelectAllClick) },
            )

            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(140.dp),
            ) {
                items(state.sessions, key = { it.connectionId }) { session ->
                    SessionListItem(
                        session = session,
                        isSelected = state.selectedSessions.contains(session.connectionId),
                        onClick = { eventPublisher(ActiveSessionsContract.UiEvent.SessionClick(session.connectionId)) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            ActionButtons(
                disconnecting = state.disconnecting,
                disconnectEnabled = state.selectedSessions.isNotEmpty(),
                onDisconnectClick = { eventPublisher(ActiveSessionsContract.UiEvent.DisconnectClick) },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HeaderSection(allSelected: Boolean, onSelectAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.nostr_connect_active_sessions_title),
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                color = AppTheme.colorScheme.onPrimary,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onSelectAllClick) {
            Text(
                color = AppTheme.colorScheme.secondary,
                text = if (allSelected) {
                    stringResource(id = R.string.nostr_connect_deselect_all_button)
                } else {
                    stringResource(id = R.string.nostr_connect_select_all_button)
                },
                style = AppTheme.typography.titleLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
            )
        }
    }
}

@Composable
private fun SessionListItem(
    session: ActiveSessionsContract.NwcSessionUi,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .selectableItem(selected = isSelected, onClick = onClick)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.medium,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UniversalAvatarThumbnail(
                avatarCdnImage = session.appImageUrl?.let { net.primal.domain.links.CdnImage(sourceUrl = it) },
                avatarSize = 38.dp,
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = session.appName ?: stringResource(id = R.string.nostr_connect_unknown_app),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = AppTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    ),
                )
                Text(
                    text = session.appUrl ?: "",
                    style = AppTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        }

        UniversalAvatarThumbnail(
            avatarCdnImage = session.userAccount.avatarCdnImage,
            avatarSize = 28.dp,
        )
    }
}

@Composable
private fun ActionButtons(
    disconnecting: Boolean,
    disconnectEnabled: Boolean,
    onDisconnectClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimalFilledButton(
            modifier = Modifier.weight(1f).height(50.dp),
            containerColor = Color.Transparent,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            border = BorderStroke(width = 1.dp, color = AppTheme.colorScheme.outline),
            textStyle = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            enabled = false,
            onClick = { },
        ) {
            Text(text = stringResource(id = R.string.nostr_connect_settings_button))
        }

        PrimalLoadingButton(
            modifier = Modifier.weight(1f).height(50.dp),
            loading = disconnecting,
            enabled = disconnectEnabled,
            text = stringResource(id = R.string.nostr_connect_disconnect_button),
            onClick = onDisconnectClick,
        )
    }
}
