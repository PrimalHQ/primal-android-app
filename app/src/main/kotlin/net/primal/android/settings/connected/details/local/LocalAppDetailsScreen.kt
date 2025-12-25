package net.primal.android.settings.connected.details.local

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.settings.connected.details.ConnectedAppDeleteDialog
import net.primal.android.settings.connected.details.ConnectedAppPermissionsSection
import net.primal.android.settings.connected.details.ConnectedAppUpdateTrustLevelDialog
import net.primal.android.settings.connected.details.DangerPrimaryColor
import net.primal.android.settings.connected.details.DangerSecondaryColor
import net.primal.android.settings.connected.details.connectedAppRecentSessionsSection
import net.primal.android.settings.connected.model.SessionUi
import net.primal.android.signer.provider.AppDisplayInfo
import net.primal.android.signer.provider.rememberAppDisplayInfo
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.account.model.TrustLevel

@Composable
fun LocalAppDetailsScreen(
    viewModel: LocalAppDetailsViewModel,
    onClose: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onPermissionDetailsClick: (identifier: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                LocalAppDetailsContract.SideEffect.ConnectionDeleted -> onClose()
            }
        }
    }

    LocalAppDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
        onSessionClick = onSessionClick,
        onPermissionDetailsClick = onPermissionDetailsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAppDetailsScreen(
    state: LocalAppDetailsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (LocalAppDetailsContract.UiEvent) -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onPermissionDetailsClick: (identifier: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val appDisplayInfo = rememberAppDisplayInfo(packageName = state.appPackageName ?: "")

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(LocalAppDetailsContract.UiEvent.DismissError) },
    )

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_connected_app_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppTheme.colorScheme.background,
        content = { paddingValues ->
            if (state.loading) {
                PrimalLoadingSpinner()
            } else {
                LocalAppDetailsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp),
                    state = state,
                    appDisplayInfo = appDisplayInfo,
                    eventPublisher = eventPublisher,
                    onSessionClick = onSessionClick,
                    onPermissionDetailsClick = { onPermissionDetailsClick(state.identifier) },
                )
            }
        },
    )
}

@Composable
fun LocalAppDetailsContent(
    modifier: Modifier = Modifier,
    state: LocalAppDetailsContract.UiState,
    appDisplayInfo: AppDisplayInfo,
    eventPublisher: (LocalAppDetailsContract.UiEvent) -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onPermissionDetailsClick: () -> Unit,
) {
    var confirmDeletionDialogVisibility by remember { mutableStateOf(false) }
    var pendingTrustLevelValue by remember { mutableStateOf<TrustLevel?>(null) }

    if (confirmDeletionDialogVisibility) {
        ConnectedAppDeleteDialog(
            onConfirm = {
                confirmDeletionDialogVisibility = false
                eventPublisher(LocalAppDetailsContract.UiEvent.DeleteConnection)
            },
            onDismiss = { confirmDeletionDialogVisibility = false },
        )
    }

    if (pendingTrustLevelValue != null) {
        ConnectedAppUpdateTrustLevelDialog(
            trustLevel = pendingTrustLevelValue!!,
            onConfirm = {
                eventPublisher(LocalAppDetailsContract.UiEvent.UpdateTrustLevel(it))
                pendingTrustLevelValue = null
            },
            onDismiss = { pendingTrustLevelValue = null },
        )
    }

    LazyColumn(modifier = modifier) {
        item(key = "Header", contentType = "Header") {
            LocalAppHeaderSection(
                modifier = Modifier.padding(vertical = 16.dp),
                appDisplayInfo = appDisplayInfo,
                lastSession = state.lastSessionStartedAt,
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

        connectedAppRecentSessionsSection(
            recentSessions = state.recentSessions,
            appIconUrl = null,
            appName = appDisplayInfo.name,
            appPackageName = state.appPackageName,
            onSessionClick = onSessionClick,
        )
    }
}

@Composable
private fun LocalAppHeaderSection(
    modifier: Modifier = Modifier,
    appDisplayInfo: AppDisplayInfo,
    lastSession: Long?,
    onDeleteConnectionClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                if (appDisplayInfo.icon != null) {
                    Image(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .size(48.dp)
                            .clip(AppTheme.shapes.small),
                        bitmap = appDisplayInfo.icon.toBitmap().asImageBitmap(),
                        contentDescription = appDisplayInfo.name,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    AppIconThumbnail(
                        modifier = Modifier.padding(bottom = 12.dp),
                        appIconUrl = null,
                        appName = appDisplayInfo.name,
                        avatarSize = 48.dp,
                    )
                }

                Text(
                    text = appDisplayInfo.name,
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

            DeleteOnlyActionButton(
                onDeleteConnectionClick = onDeleteConnectionClick,
            )
        }
    }
}

@Composable
private fun DeleteOnlyActionButton(onDeleteConnectionClick: () -> Unit) {
    val blendedContainerColor = DangerPrimaryColor.copy(
        alpha = 0.12f,
    ).compositeOver(AppTheme.colorScheme.surface)

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
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

private val mockRecentSessionsForPreview = listOf(
    SessionUi("1", 1730169240L),
    SessionUi("2", 1730157720L),
)

@Preview
@Composable
fun PreviewLocalAppDetailsScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        LocalAppDetailsScreen(
            state = LocalAppDetailsContract.UiState(
                identifier = "local_app_1",
                appPackageName = "com.example.app",
                trustLevel = TrustLevel.Medium,
                lastSessionStartedAt = System.currentTimeMillis(),
                recentSessions = mockRecentSessionsForPreview,
                loading = false,
            ),
            onClose = {},
            eventPublisher = {},
            onSessionClick = {},
            onPermissionDetailsClick = {},
        )
    }
}
