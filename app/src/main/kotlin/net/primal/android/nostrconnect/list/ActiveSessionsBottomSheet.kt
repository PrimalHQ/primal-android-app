package net.primal.android.nostrconnect.list

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.ext.selectableItem
import net.primal.android.nostrconnect.list.ActiveSessionsContract.UiState
import net.primal.android.nostrconnect.model.ActiveSessionUi
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionsBottomSheet(
    viewModel: ActiveSessionsViewModel,
    onDismissRequest: () -> Unit,
    onSettingsClick: (String?) -> Unit,
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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        ActiveSessionsContent(
            state = state,
            eventPublisher = { viewModel.setEvent(it) },
            snackbarHostState = snackbarHostState,
            onSettingsClick = {
                val selectedSessionIds = state.selectedSessions
                val connectionId = if (selectedSessionIds.size == 1) {
                    val sessionId = selectedSessionIds.first()
                    state.sessions.find { it.sessionId == sessionId }?.connectionId
                } else {
                    null
                }
                onSettingsClick(connectionId)
            },
        )
    }
}

private val SessionListItemHeight = 65.dp
private val SessionListItemsSpacedBy = 12.dp

@Composable
private fun ActiveSessionsContent(
    state: UiState,
    eventPublisher: (ActiveSessionsContract.UiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onSettingsClick: () -> Unit,
) {
    val activeSessions = state.sessions.size
    val hasActiveSessions = activeSessions > 0

    Scaffold(
        modifier = Modifier
            .height(
                height = 144.dp + if (hasActiveSessions) {
                    (SessionListItemHeight + SessionListItemsSpacedBy).times(activeSessions)
                } else {
                    80.dp
                },
            )
            .padding(horizontal = 26.dp),
        topBar = {
            HeaderSection(
                hasActiveSessions = hasActiveSessions,
                allSelected = state.allSessionsSelected,
                onSelectAllClick = { eventPublisher(ActiveSessionsContract.UiEvent.SelectAllClick) },
            )
        },
        content = { paddingValues ->
            if (hasActiveSessions) {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(bottom = 40.dp, top = 14.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(SessionListItemsSpacedBy),
                ) {
                    items(
                        items = state.sessions,
                        key = { it.sessionId },
                    ) { session ->
                        SessionListItem(
                            session = session,
                            isSelected = state.selectedSessions.contains(session.sessionId),
                            onClick = {
                                eventPublisher(
                                    ActiveSessionsContract.UiEvent.SessionClick(session.sessionId),
                                )
                            },
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.nostr_connect_no_active_sessions),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }
            }
        },
        bottomBar = {
            ActionButtons(
                disconnecting = state.disconnecting,
                disconnectEnabled = state.selectedSessions.isNotEmpty(),
                onDisconnectClick = { eventPublisher(ActiveSessionsContract.UiEvent.DisconnectClick) },
                onSettingsClick = onSettingsClick,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        containerColor = Color.Transparent,
    )
}

@Composable
private fun HeaderSection(
    hasActiveSessions: Boolean,
    allSelected: Boolean,
    onSelectAllClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.nostr_connect_active_sessions_title),
            style = AppTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = AppTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        if (hasActiveSessions) {
            TextButton(onClick = onSelectAllClick) {
                Text(
                    color = AppTheme.colorScheme.secondary,
                    text = if (allSelected) {
                        stringResource(id = R.string.nostr_connect_deselect_all_button)
                    } else {
                        stringResource(id = R.string.nostr_connect_select_all_button)
                    },
                    style = AppTheme.typography.titleMedium.copy(lineHeight = 20.sp),
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SessionListItem(
    session: ActiveSessionUi,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SessionListItemHeight)
            .selectableItem(selected = isSelected, onClick = onClick)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.medium,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppIconThumbnail(
                avatarCdnImage = session.appImageUrl?.let { CdnImage(sourceUrl = it) },
                appName = session.appName,
                avatarSize = 40.dp,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = session.appName ?: stringResource(id = R.string.nostr_connect_unknown_app),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.titleMedium.copy(lineHeight = 20.sp),
                    color = AppTheme.colorScheme.onPrimary,
                )
                Text(
                    text = session.appUrl ?: "",
                    style = AppTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        }

        UniversalAvatarThumbnail(
            avatarCdnImage = session.userAccount?.avatarCdnImage,
            legendaryCustomization = session.userAccount?.legendaryCustomization,
            avatarSize = 28.dp,
        )
    }
}

@Composable
private fun ActionButtons(
    disconnecting: Boolean,
    disconnectEnabled: Boolean,
    onDisconnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimalFilledButton(
            modifier = Modifier
                .weight(weight = 1f)
                .height(45.dp),
            containerColor = Color.Transparent,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            border = BorderStroke(width = 1.dp, color = AppTheme.colorScheme.outline),
            textStyle = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
            onClick = onSettingsClick,
        ) {
            Text(text = stringResource(id = R.string.nostr_connect_settings_button))
        }

        PrimalLoadingButton(
            modifier = Modifier
                .weight(weight = 1f)
                .height(45.dp),
            loading = disconnecting,
            enabled = disconnectEnabled,
            text = stringResource(id = R.string.nostr_connect_disconnect_button),
            onClick = onDisconnectClick,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
