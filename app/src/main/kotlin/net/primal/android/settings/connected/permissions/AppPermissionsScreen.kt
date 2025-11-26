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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.connected.model.PermissionGroupUi
import net.primal.android.settings.connected.permissions.AppPermissionsContract.UiEvent
import net.primal.android.settings.connected.ui.ConnectedAppHeader
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.PermissionAction

private val ToggleIndicatorColorDark = Color(0xFF757575)
private val ToggleIndicatorColorLight = Color(0xFF666666)
private const val PERMISSION_OPTION_COUNT = 3

@Composable
fun AppPermissionsScreen(viewModel: AppPermissionsViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()
    AppPermissionsScreen(
        state = state.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPermissionsScreen(
    state: AppPermissionsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    PrimalScaffold(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_connected_app_permissions_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
        ) {
            item {
                ConnectedAppHeader(
                    modifier = Modifier.padding(vertical = 16.dp),
                    appName = state.appName,
                    appIconUrl = state.appIconUrl,
                    startedAt = state.appLastSessionAt,
                )
            }

            itemsIndexed(
                items = state.permissions,
                key = { _, permission -> permission.groupId },
            ) { index, permission ->
                val shape = getListItemShape(index = index, listSize = state.permissions.size)

                Column(
                    modifier = Modifier.clip(shape),
                ) {
                    PermissionRow(
                        modifier = Modifier.background(AppTheme.extraColorScheme.surfaceVariantAlt3),
                        permission = permission,
                        onActionChange = {
                            eventPublisher(UiEvent.ChangePermission(permission.groupId, it))
                        },
                    )
                    if (index < state.permissions.lastIndex) {
                        PrimalDivider()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PermissionRow(
    permission: PermissionGroupUi,
    modifier: Modifier = Modifier,
    onActionChange: (PermissionAction) -> Unit,
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
            text = permission.title,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        PermissionToggle(
            currentAction = permission.action,
            onActionChange = onActionChange,
        )
    }
}

@Composable
private fun PermissionToggle(currentAction: PermissionAction, onActionChange: (PermissionAction) -> Unit) {
    val allowText = stringResource(id = R.string.settings_connected_app_permissions_allow)
    val denyText = stringResource(id = R.string.settings_connected_app_permissions_deny)
    val askText = stringResource(id = R.string.settings_connected_app_permissions_ask)

    val selectedIndex = when (currentAction) {
        PermissionAction.Approve -> 0
        PermissionAction.Deny -> 1
        PermissionAction.Ask -> 2
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
                isSelected = currentAction == PermissionAction.Approve,
                modifier = Modifier.width(fixedSegmentWidth),
                onClick = { onActionChange(PermissionAction.Approve) },
            )
            ToggleOption(
                text = denyText,
                isSelected = currentAction == PermissionAction.Deny,
                modifier = Modifier.width(fixedSegmentWidth),
                onClick = { onActionChange(PermissionAction.Deny) },
            )
            ToggleOption(
                text = askText,
                isSelected = currentAction == PermissionAction.Ask,
                modifier = Modifier.width(fixedSegmentWidth),
                onClick = { onActionChange(PermissionAction.Ask) },
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
