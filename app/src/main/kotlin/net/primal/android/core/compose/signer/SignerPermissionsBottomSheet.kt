package net.primal.android.core.compose.signer

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.PrimalBottomSheetDragHandle
import net.primal.android.core.compose.PrimalCheckBox
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.signer.ui.NostrEventDetails
import net.primal.android.core.compose.signer.ui.buildRows
import net.primal.android.core.compose.signer.ui.getStatusTextAndColor
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignerPermissionsBottomSheet(
    events: List<SessionEvent>,
    appName: String?,
    onDismissRequest: () -> Unit,
    onAllow: (eventIds: List<String>, alwaysAllow: Boolean) -> Unit,
    onReject: (eventIds: List<String>, alwaysReject: Boolean) -> Unit,
    onLookUpEventDetails: (String) -> Unit,
    onCloseEventDetails: () -> Unit,
    modifier: Modifier = Modifier,
    appIconUrl: String? = null,
    appIcon: Drawable? = null,
    permissionsMap: Map<String, String> = emptyMap(),
    eventDetails: SignerEventDetails? = null,
    responding: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
) {
    var selectedEventIds by remember { mutableStateOf(emptySet<String>()) }
    var alwaysHandleRequestsLikeThis by remember { mutableStateOf(true) }

    var previousSessionId by remember { mutableStateOf<String?>(null) }
    var previousEventIds by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(events) {
        selectedEventIds = resolveSmartSelection(
            newEvents = events,
            previousSessionId = previousSessionId,
            previousEventIds = previousEventIds,
            currentSelectedIds = selectedEventIds,
        )

        previousSessionId = events.firstOrNull()?.sessionId
        previousEventIds = events.map { it.eventId }.toSet()
    }

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = { PrimalBottomSheetDragHandle() },
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SessionDetailsColumn(
                appName = appName,
                appIconUrl = appIconUrl,
                appIcon = appIcon,
            )

            AnimatedContent(
                targetState = eventDetails,
                contentAlignment = Alignment.TopCenter,
                transitionSpec = {
                    val animationSpec = tween<IntOffset>(durationMillis = 300)
                    if (targetState != null) {
                        slideInHorizontally(animationSpec = animationSpec) { it } togetherWith
                            slideOutHorizontally(animationSpec = animationSpec) { -it }
                    } else {
                        slideInHorizontally(animationSpec = animationSpec) { -it } togetherWith
                            slideOutHorizontally(animationSpec = animationSpec) { it }
                    }
                },
                label = "PermissionsContent",
            ) { details ->
                if (details != null) {
                    EventDetailsContent(
                        sessionEvent = details.sessionEvent,
                        permissionsMap = permissionsMap,
                        parsedSignedEvent = details.parsedSignedEvent,
                        parsedUnsignedEvent = details.parsedUnsignedEvent,
                        onClose = onCloseEventDetails,
                    )
                } else {
                    PermissionsListContent(
                        events = events,
                        selectedEventIds = selectedEventIds,
                        permissionsMap = permissionsMap,
                        alwaysAllow = alwaysHandleRequestsLikeThis,
                        responding = responding,
                        onToggleAlwaysAllow = { alwaysHandleRequestsLikeThis = it },
                        onSelectAll = { selectedEventIds = events.map { it.eventId }.toSet() },
                        onDeselectAll = { selectedEventIds = emptySet() },
                        onSelectEvent = { id -> selectedEventIds = selectedEventIds + id },
                        onDeselectEvent = { id -> selectedEventIds = selectedEventIds - id },
                        onAllow = { onAllow(selectedEventIds.toList(), alwaysHandleRequestsLikeThis) },
                        onReject = { onReject(selectedEventIds.toList(), alwaysHandleRequestsLikeThis) },
                        onOpenDetails = onLookUpEventDetails,
                    )
                }
            }
        }
    }
}

data class SignerEventDetails(
    val sessionEvent: SessionEvent,
    val parsedSignedEvent: NostrEvent? = null,
    val parsedUnsignedEvent: NostrUnsignedEvent? = null,
)

@Composable
private fun PermissionsListContent(
    events: List<SessionEvent>,
    selectedEventIds: Set<String>,
    permissionsMap: Map<String, String>,
    alwaysAllow: Boolean,
    responding: Boolean,
    onToggleAlwaysAllow: (Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSelectEvent: (String) -> Unit,
    onDeselectEvent: (String) -> Unit,
    onAllow: () -> Unit,
    onReject: () -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppRequestsHeader(
            modifier = Modifier.padding(start = 24.dp, end = 16.dp),
            showSelectAll = selectedEventIds.size != events.size,
            onSelectAllClick = onSelectAll,
            onDeselectAllClick = onDeselectAll,
        )

        AppRequestsList(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .padding(horizontal = 24.dp),
            events = events,
            permissionsMap = permissionsMap,
            selectedEventIds = selectedEventIds,
            onSelectEventClick = onSelectEvent,
            onDeselectEventClick = onDeselectEvent,
            onOpenDetailsClick = onOpenDetails,
        )

        AlwaysHandleRequestsSwitch(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            checked = alwaysAllow,
            onCheckedChange = onToggleAlwaysAllow,
        )

        ActionButtons(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            enabled = !responding && selectedEventIds.isNotEmpty(),
            onRejectClick = onReject,
            onAllowClick = onAllow,
        )
    }
}

private const val EVENT_DETAILS_MAX_HEIGHT_RATIO = 0.8f

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

    Column(modifier = Modifier.fillMaxHeight(EVENT_DETAILS_MAX_HEIGHT_RATIO)) {
        NostrEventDetails(
            modifier = Modifier.weight(1f),
            title = actionName,
            subtitle = formattedTimestamp,
            rows = rows,
            status = status,
            statusColor = color,
            onCopy = { text, label ->
                copyText(context = context, text = text, label = label)
            },
        )

        EventDetailsBackButton(
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = onClose,
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
private fun AppRequestListItem(
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
private fun SessionDetailsColumn(
    modifier: Modifier = Modifier,
    appName: String?,
    appIconUrl: String?,
    appIcon: Drawable? = null,
) {
    Column(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (appIcon != null) {
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppTheme.shapes.small),
                bitmap = appIcon.toBitmap().asImageBitmap(),
                contentDescription = appName,
                contentScale = ContentScale.Crop,
            )
        } else {
            AppIconThumbnail(
                avatarSize = 40.dp,
                appIconUrl = appIconUrl,
                appName = appName,
            )
        }

        Text(
            text = appName ?: stringResource(id = R.string.permissions_bottom_sheet_unknown_app),
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onPrimary,
        )
    }
    PrimalDivider()
}

private fun resolveSmartSelection(
    newEvents: List<SessionEvent>,
    previousSessionId: String?,
    previousEventIds: Set<String>,
    currentSelectedIds: Set<String>,
): Set<String> {
    val newSessionId = newEvents.firstOrNull()?.sessionId
    val newEventIds = newEvents.map { it.eventId }.toSet()

    return if (newSessionId != previousSessionId) {
        newEventIds
    } else {
        newEventIds.filter { eventId ->
            val isOldEvent = eventId in previousEventIds
            if (isOldEvent) {
                eventId in currentSelectedIds
            } else {
                true
            }
        }.toSet()
    }
}
