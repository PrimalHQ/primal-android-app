package net.primal.android.settings.connected.permissions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.settings.connected.model.PermissionGroupUi
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.AppPermissionAction

private val ToggleIndicatorColorDark = Color(0xFF757575)
private val ToggleIndicatorColorLight = Color(0xFF666666)
private const val PERMISSION_OPTION_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedAppPermissionsScreen(
    headerContent: @Composable () -> Unit,
    permissions: List<PermissionGroupUi>,
    loading: Boolean,
    error: UiError?,
    onUpdatePermission: (List<String>, AppPermissionAction) -> Unit,
    onResetPermissions: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var resetConfirmationVisible by remember { mutableStateOf(false) }

    SnackbarErrorHandler(
        error = if (permissions.isNotEmpty()) error else null,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = context) },
        onErrorDismiss = onDismissError,
    )

    PrimalScaffold(
        modifier = Modifier.imePadding(),
        containerColor = AppTheme.colorScheme.surfaceVariant,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_connected_app_permissions_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        if (permissions.isNotEmpty()) {
            PermissionsList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                headerContent = headerContent,
                permissions = permissions,
                onUpdatePermission = onUpdatePermission,
                onResetClick = { resetConfirmationVisible = true },
            )
        } else if (loading) {
            PrimalLoadingSpinner()
        } else {
            ListNoContent(
                modifier = Modifier.fillMaxSize(),
                noContentText = stringResource(id = R.string.settings_connected_app_permissions_error_loading),
                refreshButtonVisible = true,
                onRefresh = onRetry,
            )
        }
    }

    if (resetConfirmationVisible) {
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_connected_app_permissions_reset_dialog_title),
            dialogText = stringResource(id = R.string.settings_connected_app_permissions_reset_dialog_text),
            confirmText = stringResource(id = R.string.settings_connected_app_permissions_reset_dialog_confirm),
            onConfirmation = {
                onResetPermissions()
                resetConfirmationVisible = false
            },
            dismissText = stringResource(id = R.string.settings_connected_app_permissions_reset_dialog_dismiss),
            onDismissRequest = { resetConfirmationVisible = false },
        )
    }
}

@Composable
private fun PermissionsList(
    headerContent: @Composable () -> Unit,
    permissions: List<PermissionGroupUi>,
    onUpdatePermission: (List<String>, AppPermissionAction) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            headerContent()
        }

        itemsIndexed(
            items = permissions,
            key = { _, group -> group.groupId },
        ) { index, permissionGroup ->
            val shape = getListItemShape(index = index, listSize = permissions.size)

            Column(modifier = Modifier.clip(shape)) {
                PermissionGroupRow(
                    modifier = Modifier.background(AppTheme.extraColorScheme.surfaceVariantAlt3),
                    permissionGroup = permissionGroup,
                    onActionChange = { action ->
                        onUpdatePermission(permissionGroup.permissionIds, action)
                    },
                )
                if (index < permissions.lastIndex) {
                    PrimalDivider()
                }
            }
        }

        item {
            Text(
                modifier = Modifier
                    .clickable { onResetClick() }
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                text = stringResource(id = R.string.settings_connected_app_permissions_reset),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.secondary,
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionGroupRow(
    permissionGroup: PermissionGroupUi,
    modifier: Modifier = Modifier,
    onActionChange: (AppPermissionAction) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            text = permissionGroup.title,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        PermissionToggle(
            currentAction = permissionGroup.action,
            onActionChange = onActionChange,
        )
    }
}

@Composable
private fun PermissionToggle(currentAction: AppPermissionAction, onActionChange: (AppPermissionAction) -> Unit) {
    val allowText = stringResource(id = R.string.settings_connected_app_permissions_allow)
    val denyText = stringResource(id = R.string.settings_connected_app_permissions_deny)
    val askText = stringResource(id = R.string.settings_connected_app_permissions_ask)

    val selectedIndex = when (currentAction) {
        AppPermissionAction.Approve -> 0
        AppPermissionAction.Deny -> 1
        AppPermissionAction.Ask -> 2
    }

    val fixedSegmentWidth = 55.dp
    val padding = 2.dp
    val totalWidth = (fixedSegmentWidth * PERMISSION_OPTION_COUNT) + (padding * 2)

    Box(
        modifier = Modifier
            .width(totalWidth)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colorScheme.background)
            .padding(padding),
    ) {
        val indicatorOffset by animateDpAsState(
            targetValue = fixedSegmentWidth * selectedIndex,
            animationSpec = tween(durationMillis = 250),
            label = "indicatorOffset",
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(fixedSegmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (LocalPrimalTheme.current.isDarkTheme) ToggleIndicatorColorDark else ToggleIndicatorColorLight,
                ),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ToggleOption(
                text = allowText,
                isSelected = currentAction == AppPermissionAction.Approve,
                modifier = Modifier.width(fixedSegmentWidth),
                onClick = { onActionChange(AppPermissionAction.Approve) },
            )
            ToggleOption(
                text = denyText,
                isSelected = currentAction == AppPermissionAction.Deny,
                modifier = Modifier.width(fixedSegmentWidth),
                onClick = { onActionChange(AppPermissionAction.Deny) },
            )
            ToggleOption(
                text = askText,
                isSelected = currentAction == AppPermissionAction.Ask,
                modifier = Modifier.width(fixedSegmentWidth),
                onClick = { onActionChange(AppPermissionAction.Ask) },
            )
        }
    }
}

@Composable
private fun ToggleOption(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        animationSpec = tween(durationMillis = 200),
        label = "textColor",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = textColor,
        )
    }
}
