package net.primal.android.settings.connected.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.HighSecurity
import net.primal.android.core.compose.icons.primaliconpack.LowSecurity
import net.primal.android.core.compose.icons.primaliconpack.MediumSecurity
import net.primal.android.core.compose.nostrconnect.PermissionsListItem
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.SideEffect
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.UiEvent
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.UiState
import net.primal.android.settings.connected.model.SessionUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.links.CdnImage

private val DangerPrimaryColor = Color(0xFFFF2121)
private val DangerSecondaryColor = Color(0xFFFA3C3C)
private val EditButtonContainerColorDark = Color(0xFF333333)
private val EditButtonContainerColorLight = Color(0xFFD5D5D5)

@Composable
fun ConnectedAppDetailsScreen(
    viewModel: ConnectedAppDetailsViewModel,
    onClose: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onPermissionDetailsClick: (clientPubKey: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                SideEffect.ConnectionDeleted -> onClose()
            }
        }
    }

    ConnectedAppDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
        onSessionClick = onSessionClick,
        onPermissionDetailsClick = onPermissionDetailsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedAppDetailsScreen(
    state: UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onPermissionDetailsClick: (clientPubKey: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_connected_app_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            if (state.loading) {
                PrimalLoadingSpinner()
            } else {
                ConnectedAppDetailsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp),
                    state = state,
                    eventPublisher = eventPublisher,
                    onSessionClick = onSessionClick,
                    onPermissionDetailsClick = { onPermissionDetailsClick(state.clientPubKey) },
                )
            }
        },
    )
}

@Composable
fun ConnectedAppDetailsContent(
    modifier: Modifier = Modifier,
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onPermissionDetailsClick: () -> Unit,
) {
    var confirmDeletionDialogVisibility by remember { mutableStateOf(false) }
    if (confirmDeletionDialogVisibility) {
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_connected_app_details_delete_connection_dialog_title),
            dialogText = stringResource(id = R.string.settings_connected_app_details_delete_connection_dialog_text),
            confirmText = stringResource(id = R.string.settings_connected_app_details_delete_connection_confirm),
            onConfirmation = {
                confirmDeletionDialogVisibility = false
                eventPublisher(UiEvent.DeleteConnection)
            },
            dismissText = stringResource(id = R.string.settings_connected_app_details_delete_connection_dismiss),
            onDismissRequest = { confirmDeletionDialogVisibility = false },
        )
    }

    var editingNameDialogVisibility by remember { mutableStateOf(false) }
    if (editingNameDialogVisibility) {
        EditNameAlertDialog(
            currentName = state.appName,
            onNameChange = {
                eventPublisher(UiEvent.EditName(it))
                editingNameDialogVisibility = false
            },
            onDismiss = { editingNameDialogVisibility = false },
        )
    }

    var pendingTrustLevelValue by remember { mutableStateOf<TrustLevel?>(null) }
    pendingTrustLevelValue?.let { trustLevel ->
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_connected_app_details_update_trust_level_dialog_title),
            dialogText = stringResource(
                id = R.string.settings_connected_app_details_update_trust_level_dialog_text,
                trustLevel.toUserFriendlyText(),
            ),
            confirmText = stringResource(id = R.string.settings_connected_app_details_update_trust_level_confirm),
            dismissText = stringResource(id = R.string.settings_connected_app_details_update_trust_level_dismiss),
            onConfirmation = {
                eventPublisher(UiEvent.UpdateTrustLevel(trustLevel))
                pendingTrustLevelValue = null
            },
            onDismissRequest = { pendingTrustLevelValue = null },
        )
    }

    LazyColumn(modifier = modifier) {
        item(key = "Header", contentType = "Header") {
            HeaderSection(
                modifier = Modifier.padding(vertical = 16.dp),
                iconUrl = state.appIconUrl,
                appName = state.appName,
                lastSession = state.lastSessionStartedAt,
                isSessionActive = state.isSessionActive,
                autoStartSession = state.autoStartSession,
                onAutoStartSessionChange = { eventPublisher(UiEvent.AutoStartSessionChange(it)) },
                onStartSessionClick = { eventPublisher(UiEvent.StartSession) },
                onEndSessionClick = { eventPublisher(UiEvent.EndSession) },
                onEditNameClick = { editingNameDialogVisibility = true },
                onDeleteConnectionClick = { confirmDeletionDialogVisibility = true },
            )
        }

        item(key = "Permissions", contentType = "Permissions") {
            ConnectedAppPermissionsSection(
                trustLevel = state.trustLevel,
                onTrustLevelChange = {
                    if (state.trustLevel != it) {
                        pendingTrustLevelValue = it
                    }
                },
                onPermissionDetailsClick = onPermissionDetailsClick,
            )
        }

        item(key = "Spacer", contentType = "Spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }

        recentSessionsSection(
            state = state,
            onSessionClick = onSessionClick,
        )
    }
}

private fun LazyListScope.recentSessionsSection(state: UiState, onSessionClick: (String) -> Unit) {
    if (state.recentSessions.isNotEmpty()) {
        item(key = "RecentSessionsTitle", contentType = "Title") {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(id = R.string.settings_connected_app_details_recent_sessions).uppercase(),
                style = AppTheme.typography.titleMedium.copy(lineHeight = 20.sp),
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onPrimary,
            )
        }

        itemsIndexed(
            items = state.recentSessions,
            key = { _, session -> session.sessionId },
            contentType = { _, _ -> "SessionItem" },
        ) { index, session ->
            val shape = getListItemShape(index = index, listSize = state.recentSessions.size)
            val isLast = index == state.recentSessions.lastIndex

            Column(modifier = Modifier.clip(shape)) {
                RecentSessionItem(
                    session = session,
                    iconUrl = state.appIconUrl,
                    appName = state.appName,
                    onClick = { onSessionClick(session.sessionId) },
                )
                if (!isLast) {
                    PrimalDivider()
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    modifier: Modifier = Modifier,
    iconUrl: String?,
    appName: String?,
    lastSession: Long?,
    isSessionActive: Boolean,
    autoStartSession: Boolean,
    onAutoStartSessionChange: (Boolean) -> Unit,
    onStartSessionClick: () -> Unit,
    onEndSessionClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onDeleteConnectionClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
    ) {
        Column {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppSummarySection(
                    iconUrl = iconUrl,
                    appName = appName,
                    lastSession = lastSession,
                )

                SessionControlButton(
                    isSessionActive = isSessionActive,
                    onStart = onStartSessionClick,
                    onEnd = onEndSessionClick,
                )

                AppActionButtons(
                    onEditNameClick = onEditNameClick,
                    onDeleteConnectionClick = onDeleteConnectionClick,
                )
            }

            PrimalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onAutoStartSessionChange(!autoStartSession) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.settings_connected_app_details_auto_start_session),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    fontWeight = FontWeight.Normal,
                )
                PrimalSwitch(
                    checked = autoStartSession,
                    onCheckedChange = onAutoStartSessionChange,
                )
            }
        }
    }
}

@Composable
private fun AppSummarySection(
    iconUrl: String?,
    appName: String?,
    lastSession: Long?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        AppIconThumbnail(
            modifier = Modifier.padding(bottom = 12.dp),
            avatarCdnImage = iconUrl?.let { CdnImage(it) },
            appName = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
            avatarSize = 48.dp,
        )

        Text(
            text = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
            style = AppTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            fontWeight = FontWeight.Bold,
        )
        if (lastSession != null) {
            val formattedLastSession = rememberPrimalFormattedDateTime(
                timestamp = lastSession,
                format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
            )
            Text(
                text = stringResource(
                    id = R.string.settings_connected_app_details_last_session,
                    formattedLastSession,
                ),
                style = AppTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}

@Composable
private fun SessionControlButton(
    isSessionActive: Boolean,
    onStart: () -> Unit,
    onEnd: () -> Unit,
) {
    val text = if (isSessionActive) {
        stringResource(id = R.string.settings_connected_app_details_end_session)
    } else {
        stringResource(id = R.string.settings_connected_app_details_start_session)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = AppTheme.colorScheme.onSurface,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { if (isSessionActive) onEnd() else onStart() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.surface,
        )
    }
}

@Composable
fun ConnectedAppPermissionsSection(
    trustLevel: TrustLevel,
    onTrustLevelChange: (TrustLevel) -> Unit,
    onPermissionDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.settings_connected_app_permissions_subtitle).uppercase(),
            style = AppTheme.typography.titleMedium.copy(lineHeight = 20.sp),
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onPrimary,
        )

        Column(
            modifier = Modifier.clip(AppTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PermissionsListItem(
                icon = PrimalIcons.HighSecurity,
                title = stringResource(id = R.string.signer_connect_full_trust_title),
                subtitle = stringResource(id = R.string.settings_connected_app_permissions_full_trust_subtitle),
                isSelected = trustLevel == TrustLevel.Full,
                onClick = { onTrustLevelChange(TrustLevel.Full) },
            )
            PermissionsListItem(
                icon = PrimalIcons.MediumSecurity,
                title = stringResource(id = R.string.signer_connect_medium_trust_title),
                subtitle = stringResource(id = R.string.settings_connected_app_permissions_medium_trust_subtitle),
                isSelected = trustLevel == TrustLevel.Medium,
                onClick = { onTrustLevelChange(TrustLevel.Medium) },
            )
            PermissionsListItem(
                icon = PrimalIcons.LowSecurity,
                title = stringResource(id = R.string.signer_connect_low_trust_title),
                subtitle = stringResource(id = R.string.settings_connected_app_permissions_low_trust_subtitle),
                isSelected = trustLevel == TrustLevel.Low,
                onClick = { onTrustLevelChange(TrustLevel.Low) },
            )
        }

        if (trustLevel == TrustLevel.Medium) {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onPermissionDetailsClick() },
                text = stringResource(id = R.string.settings_connected_app_permission_details_link),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun AppActionButtons(onEditNameClick: () -> Unit, onDeleteConnectionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PrimalFilledButton(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            containerColor = if (LocalPrimalTheme.current.isDarkTheme) {
                EditButtonContainerColorDark
            } else {
                EditButtonContainerColorLight
            },
            contentColor = AppTheme.colorScheme.onSurface,
            onClick = onEditNameClick,
            shape = AppTheme.shapes.extraLarge,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_connected_app_details_edit_name),
                style = AppTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val blendedContainerColor = DangerPrimaryColor.copy(
            alpha = 0.12f,
        ).compositeOver(AppTheme.colorScheme.surface)

        OutlinedButton(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            onClick = onDeleteConnectionClick,
            shape = AppTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, DangerSecondaryColor.copy(alpha = 0.2f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = blendedContainerColor,
                contentColor = DangerSecondaryColor,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_connected_app_details_delete_connection),
                style = AppTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecentSessionItem(
    session: SessionUi,
    iconUrl: String?,
    appName: String?,
    onClick: () -> Unit,
) {
    val formattedDate = rememberPrimalFormattedDateTime(
        timestamp = session.startedAt,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
    )

    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
        leadingContent = {
            AppIconThumbnail(
                avatarCdnImage = iconUrl?.let { CdnImage(it) },
                appName = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                avatarSize = 24.dp,
            )
        },
        headlineContent = {
            Text(
                text = formattedDate,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
    )
}

@Composable
private fun EditNameAlertDialog(
    currentName: String?,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
) {
    var newName by remember { mutableStateOf(currentName ?: "") }
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        title = {
            Text(text = stringResource(id = R.string.settings_connected_app_details_edit_name_dialog_title))
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onNameChange(newName) }),
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onNameChange(newName) }) {
                Text(text = stringResource(id = R.string.settings_connected_app_details_edit_name_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.settings_connected_app_details_edit_name_dismiss))
            }
        },
    )
}

@Composable
private fun TrustLevel.toUserFriendlyText() =
    when (this) {
        TrustLevel.Full -> stringResource(id = R.string.settings_connected_app_details_full_trust)
        TrustLevel.Medium -> stringResource(id = R.string.settings_connected_app_details_medium_trust)
        TrustLevel.Low -> stringResource(id = R.string.settings_connected_app_details_low_trust)
    }

private val mockRecentSessionsForPreview = listOf(
    // Oct 28, 2025 12:34 PM
    SessionUi("1", 1730169240L),
    // Oct 28, 2025 9:22 AM
    SessionUi("2", 1730157720L),
    // Oct 27, 2025 4:21 PM
    SessionUi("3", 1730091660L),
    // Oct 27, 2025 10:02 AM
    SessionUi("4", 1730068920L),
    // Oct 26, 2025 9:32 AM
    SessionUi("5", 1729980720L),
    // Oct 23, 2025 2:07 PM
    SessionUi("6", 1729728420L),
    // Oct 21, 2025 11:33 PM
    SessionUi("7", 1729560780L),
    // Oct 18, 2025 6:47 PM
    SessionUi("8", 1729298820L),
)

@Preview
@Composable
fun PreviewConnectedAppDetailsScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Ice) {
        ConnectedAppDetailsScreen(
            state = UiState(
                loading = false,
                appName = "Highlighter",
                lastSessionStartedAt = mockRecentSessionsForPreview.first().startedAt,
                recentSessions = mockRecentSessionsForPreview,
                clientPubKey = "",
            ),
            onClose = {},
            eventPublisher = {},
            onSessionClick = {},
            onPermissionDetailsClick = {},
        )
    }
}
