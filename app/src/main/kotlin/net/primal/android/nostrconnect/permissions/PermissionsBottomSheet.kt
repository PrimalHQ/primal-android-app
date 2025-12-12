package net.primal.android.nostrconnect.permissions

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.uuid.ExperimentalUuidApi
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.PrimalCheckBox
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.nostrconnect.model.ActiveSessionUi
import net.primal.android.nostrconnect.permissions.PermissionsContract.UiEvent
import net.primal.android.nostrconnect.permissions.PermissionsContract.UiState
import net.primal.android.nostrconnect.ui.NostrConnectBottomSheetDragHandle
import net.primal.android.nostrconnect.ui.NostrEventDetails
import net.primal.android.nostrconnect.ui.buildRows
import net.primal.android.nostrconnect.ui.getStatusTextAndColor
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.links.CdnImage
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsBottomSheet(
    viewModel: PermissionsViewModel,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    if (state.bottomSheetVisibility) {
        ModalBottomSheet(
            modifier = Modifier.padding(top = 100.dp),
            sheetState = sheetState,
            dragHandle = { NostrConnectBottomSheetDragHandle() },
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            onDismissRequest = { viewModel.setEvent(UiEvent.DismissSheet) },
        ) {
            PermissionsBottomSheetContent(
                uiState = state,
                eventPublisher = viewModel::setEvent,
            )
        }
    }

    content()
}

@Composable
fun PermissionsBottomSheetContent(uiState: UiState, eventPublisher: (UiEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        uiState.session?.let {
            SessionDetailsColumn(session = it)
        }

        AnimatedContent(
            targetState = uiState.eventDetailsSessionEvent,
            contentAlignment = Alignment.TopCenter,
            transitionSpec = {
                val animationSpec = tween<IntOffset>(durationMillis = 300)

                val transition = if (targetState != null) {
                    slideInHorizontally(animationSpec = animationSpec) { it } togetherWith
                        slideOutHorizontally(animationSpec = animationSpec) { -it }
                } else {
                    slideInHorizontally(animationSpec = animationSpec) { -it } togetherWith
                        slideOutHorizontally(animationSpec = animationSpec) { it }
                }

                transition.using(
                    SizeTransform(clip = false),
                )
            },
            label = "PermissionsContent",
        ) { sessionEvent ->
            if (sessionEvent != null) {
                EventDetailsContent(
                    sessionEvent = sessionEvent,
                    permissionsMap = uiState.permissionsMap,
                    parsedSignedEvent = uiState.parsedSignedEvent,
                    parsedUnsignedEvent = uiState.parsedUnsignedEvent,
                    onClose = { eventPublisher(UiEvent.CloseEventDetails) },
                )
            } else {
                PermissionsListContent(
                    uiState = uiState,
                    eventPublisher = eventPublisher,
                )
            }
        }
    }
}

@Composable
private fun PermissionsListContent(uiState: UiState, eventPublisher: (UiEvent) -> Unit) {
    var alwaysHandleRequestsLikeThis by remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppRequestsHeader(
            modifier = Modifier.padding(start = 24.dp, end = 16.dp),
            showSelectAll = uiState.selectedEventIds.size != uiState.sessionEvents.size,
            onSelectAllClick = { eventPublisher(UiEvent.SelectAll) },
            onDeselectAllClick = { eventPublisher(UiEvent.DeselectAll) },
        )

        AppRequestsList(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .padding(horizontal = 24.dp),
            events = uiState.sessionEvents,
            permissionsMap = uiState.permissionsMap,
            selectedEventIds = uiState.selectedEventIds,
            onSelectEventClick = { eventPublisher(UiEvent.SelectEvent(it)) },
            onDeselectEventClick = { eventPublisher(UiEvent.DeselectEvent(it)) },
            onOpenDetailsClick = { eventPublisher(UiEvent.OpenEventDetails(it)) },
        )

        AlwaysHandleRequestsSwitch(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            checked = alwaysHandleRequestsLikeThis,
            onCheckedChange = { alwaysHandleRequestsLikeThis = it },
        )

        ActionButtons(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            enabled = !uiState.responding && uiState.selectedEventIds.isNotEmpty(),
            onRejectClick = {
                eventPublisher(UiEvent.RejectSelected(alwaysReject = alwaysHandleRequestsLikeThis))
            },
            onAllowClick = {
                eventPublisher(UiEvent.AllowSelected(alwaysAllow = alwaysHandleRequestsLikeThis))
            },
        )
    }
}

@Composable
private fun EventDetailsContent(
    sessionEvent: SessionEvent,
    permissionsMap: Map<String, String>,
    parsedSignedEvent: NostrEvent?,
    parsedUnsignedEvent: NostrUnsignedEvent?,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val actionName = permissionsMap[sessionEvent.requestTypeId] ?: sessionEvent.requestTypeId

    val formattedTimestamp = rememberPrimalFormattedDateTime(
        timestamp = sessionEvent.requestedAt,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
    )

    val (status, color) = getStatusTextAndColor(context, sessionEvent)

    val rows = remember(sessionEvent, permissionsMap, parsedSignedEvent, parsedUnsignedEvent) {
        buildRows(
            context = context,
            event = sessionEvent,
            namingMap = permissionsMap,
            parsedSignedEvent = parsedSignedEvent,
            parsedUnsignedEvent = parsedUnsignedEvent,
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        NostrEventDetails(
            title = actionName,
            subtitle = formattedTimestamp,
            rows = rows,
            status = status,
            statusColor = color,
            onCopy = { text, label ->
                copyText(context = context, text = text, label = label)
            },
            footerContent = {
                EventDetailsBackButton(onClick = onClose)
            },
        )
    }
}

@Composable
private fun EventDetailsBackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    PrimalFilledButton(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(50.dp),
        onClick = onClick,
    ) {
        Text(text = stringResource(id = R.string.permissions_bottom_sheet_back_button))
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onRejectClick: () -> Unit,
    onAllowClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrimalFilledButton(
            modifier = Modifier.weight(1f),
            height = 46.dp,
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
            contentColor = AppTheme.colorScheme.onPrimary,
            border = BorderStroke(width = 1.dp, color = AppTheme.colorScheme.outline),
            textStyle = AppTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            onClick = onRejectClick,
            enabled = enabled,
        ) {
            Text(
                text = stringResource(id = R.string.permissions_bottom_sheet_reject_selected),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        PrimalFilledButton(
            modifier = Modifier.weight(1f),
            height = 46.dp,
            onClick = onAllowClick,
            contentPadding = PaddingValues(horizontal = 16.dp),
            textStyle = AppTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            enabled = enabled,
        ) {
            Text(
                text = stringResource(id = R.string.permissions_bottom_sheet_allow_selected),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AlwaysHandleRequestsSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = RoundedCornerShape(size = 12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onCheckedChange(!checked) },
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.permissions_bottom_sheet_always_handle),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
        )

        PrimalSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun AppRequestsList(
    modifier: Modifier = Modifier,
    events: List<SessionEvent>,
    permissionsMap: Map<String, String>,
    selectedEventIds: Set<String>,
    onSelectEventClick: (String) -> Unit,
    onDeselectEventClick: (String) -> Unit,
    onOpenDetailsClick: (String) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(
            items = events,
            key = { _, item -> item.eventId },
        ) { index, event ->
            AppRequestListItem(
                shape = getListItemShape(index = index, listSize = events.size),
                event = event,
                permissionsMap = permissionsMap,
                isSelected = event.eventId in selectedEventIds,
                onSelectClick = { onSelectEventClick(event.eventId) },
                onDeselectClick = { onDeselectEventClick(event.eventId) },
                showDivider = index < events.lastIndex,
                onDetailsClick = { onOpenDetailsClick(event.eventId) },
            )
        }
    }
}

@Composable
fun AppRequestListItem(
    modifier: Modifier = Modifier,
    event: SessionEvent,
    shape: Shape,
    isSelected: Boolean,
    permissionsMap: Map<String, String>,
    onSelectClick: () -> Unit,
    onDeselectClick: () -> Unit,
    showDivider: Boolean,
    onDetailsClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .clickable(onClick = onDetailsClick)
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PrimalCheckBox(
            size = DpSize(width = 20.dp, height = 20.dp),
            checked = isSelected,
            onCheckedChange = {
                if (it) {
                    onSelectClick()
                } else {
                    onDeselectClick()
                }
            },
        )

        SessionEventMetadata(
            name = permissionsMap[event.requestTypeId] ?: event.requestTypeId,
            requestedAt = event.requestedAt,
        )
    }

    if (showDivider) {
        PrimalDivider()
    }
}

@Composable
private fun SessionEventMetadata(name: String, requestedAt: Long) {
    val formattedDateTime = rememberPrimalFormattedDateTime(
        timestamp = requestedAt,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = name,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.onPrimary,
            )

            Text(
                text = formattedDateTime,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }

        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}

@Composable
private fun AppRequestsHeader(
    modifier: Modifier = Modifier,
    showSelectAll: Boolean,
    onSelectAllClick: () -> Unit,
    onDeselectAllClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.permissions_bottom_sheet_app_requests),
            style = AppTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        TextButton(
            onClick = {
                if (showSelectAll) {
                    onSelectAllClick()
                } else {
                    onDeselectAllClick()
                }
            },
        ) {
            Text(
                text = stringResource(
                    id = if (showSelectAll) {
                        R.string.permissions_bottom_sheet_select_all
                    } else {
                        R.string.permissions_bottom_sheet_deselect_all
                    },
                ),
                style = AppTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = AppTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun SessionDetailsColumn(modifier: Modifier = Modifier, session: ActiveSessionUi) {
    Column(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIconThumbnail(
            avatarSize = 40.dp,
            avatarCdnImage = session.appImageUrl?.let { CdnImage(sourceUrl = it) },
            appName = session.appName,
        )

        Text(
            text = session.appName ?: stringResource(id = R.string.permissions_bottom_sheet_unknown_app),
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onPrimary,
        )
    }
    PrimalDivider()
}
