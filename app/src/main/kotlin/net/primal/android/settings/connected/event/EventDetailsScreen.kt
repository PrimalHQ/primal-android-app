package net.primal.android.settings.connected.event

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.settings.connected.event.EventDetailsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

private const val CONTENT_MAX_LINES_COLLAPSED = 10

private sealed interface EventDetailRow {
    data class Detail(
        val label: String,
        val value: String,
        val singleLine: Boolean = true,
        val expandable: Boolean = false,
    ) : EventDetailRow

    data class Tags(
        val label: String,
        val tags: List<JsonArray>,
    ) : EventDetailRow
}

@Composable
fun EventDetailsScreen(viewModel: EventDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.effect.collect {
            when (it) {
                is EventDetailsContract.SideEffect.TextCopied -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_event_details_copied_toast, it.label),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    EventDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    state: EventDetailsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_event_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            if (state.loading) {
                PrimalLoadingSpinner()
            } else if (state.event != null) {
                val eventDetailRows = buildEventDetailRows(event = state.event)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                ) {
                    item(key = "Header", contentType = "Header") {
                        EventDetailsHeader(
                            eventKind = state.event.kind,
                            timestamp = state.event.createdAt,
                        )
                    }

                    itemsIndexed(
                        items = eventDetailRows,
                        key = { _, item ->
                            when (item) {
                                is EventDetailRow.Detail -> item.label
                                is EventDetailRow.Tags -> item.label
                            }
                        },
                    ) { index, item ->
                        val isFirst = index == 0
                        val isLast = index == eventDetailRows.lastIndex
                        val shape = when {
                            isFirst && isLast -> RoundedCornerShape(size = 12.dp)
                            isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                            else -> RectangleShape
                        }

                        Column(
                            modifier = Modifier
                                .clip(shape)
                                .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
                        ) {
                            when (item) {
                                is EventDetailRow.Detail -> EventDetailListItem(
                                    label = item.label,
                                    value = item.value,
                                    onCopy = {
                                        copyText(context = context, text = item.value, label = item.label)
                                        eventPublisher(UiEvent.CopyToClipboard(item.value, item.label))
                                    },
                                    singleLine = item.singleLine,
                                    expandable = item.expandable,
                                )

                                is EventDetailRow.Tags -> TagsListItem(
                                    label = item.label,
                                    tags = item.tags,
                                    onCopy = { textToCopy ->
                                        copyText(context = context, text = textToCopy, label = item.label)
                                        eventPublisher(UiEvent.CopyToClipboard(textToCopy, item.label))
                                    },
                                )
                            }
                            if (!isLast) {
                                PrimalDivider()
                            }
                        }
                    }

                    item(key = "CopyRawJsonButton", contentType = "Button") {
                        val rawJsonLabel = stringResource(id = R.string.settings_event_details_raw_json_label)
                        PrimalFilledButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .height(50.dp),
                            onClick = {
                                state.rawJson?.let {
                                    copyText(context = context, text = it, label = rawJsonLabel)
                                    eventPublisher(UiEvent.CopyToClipboard(it, rawJsonLabel))
                                }
                            },
                        ) {
                            Text(text = stringResource(id = R.string.settings_event_details_copy_raw_json))
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun buildEventDetailRows(event: NostrEvent): List<EventDetailRow> {
    return listOf(
        EventDetailRow.Detail(
            label = stringResource(id = R.string.settings_event_details_id_label),
            value = event.id,
        ),
        EventDetailRow.Detail(
            label = stringResource(id = R.string.settings_event_details_pubkey_label),
            value = event.pubKey,
        ),
        EventDetailRow.Detail(
            label = stringResource(id = R.string.settings_event_details_event_kind_label),
            value = "${event.kind} - ${event.kind.toKindName()}",
        ),
        EventDetailRow.Detail(
            label = stringResource(id = R.string.settings_event_details_created_at_label),
            value = event.createdAt.toString(),
        ),
        EventDetailRow.Tags(
            label = stringResource(id = R.string.settings_event_details_tags_label),
            tags = event.tags,
        ),
        EventDetailRow.Detail(
            label = stringResource(id = R.string.settings_event_details_content_label),
            value = event.content,
            singleLine = false,
            expandable = true,
        ),
        EventDetailRow.Detail(
            label = stringResource(id = R.string.settings_event_details_signature_label),
            value = event.sig,
        ),
    )
}

@Composable
private fun EventDetailsHeader(eventKind: Int, timestamp: Long) {
    val formattedTimestamp = rememberPrimalFormattedDateTime(
        timestamp = timestamp,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = eventKind.toKindTitle(),
            style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = formattedTimestamp,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
    }
}

@Composable
private fun EventDetailListItem(
    label: String,
    value: String,
    onCopy: () -> Unit,
    singleLine: Boolean = true,
    expandable: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showToggle by remember { mutableStateOf(false) }

    val maxLines = if (singleLine) 1 else if (expandable && !isExpanded) CONTENT_MAX_LINES_COLLAPSED else Int.MAX_VALUE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier.padding(top = 7.dp),
                text = value,
                style = AppTheme.typography.bodySmall.copy(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    lineHeight = 20.sp,
                ),
                maxLines = maxLines,
                overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
                onTextLayout = {
                    if (expandable) {
                        showToggle = it.didOverflowHeight || isExpanded
                    }
                },
            )

            AnimatedVisibility(visible = showToggle) {
                Text(
                    text = if (isExpanded) {
                        stringResource(id = R.string.settings_event_details_show_less)
                    } else {
                        stringResource(id = R.string.settings_event_details_show_more)
                    },
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { isExpanded = !isExpanded },
                )
            }
        }
        Icon(
            imageVector = PrimalIcons.Copy,
            contentDescription = stringResource(id = R.string.accessibility_copy_content),
            modifier = Modifier
                .padding(start = 16.dp)
                .clickable { onCopy() },
        )
    }
}

@Composable
private fun TagsListItem(
    label: String,
    tags: List<JsonArray>,
    onCopy: (String) -> Unit,
) {
    val tagsAsStringForCopy = remember(tags) {
        tags.joinToString(separator = ",\n") { jsonArray ->
            val tagElements = jsonArray.map { jsonElement ->
                "\"${jsonElement.jsonPrimitive.content}\""
            }
            "[${tagElements.joinToString(separator = ", ")}]"
        }
    }

    val maxTagsToShow = 10
    val tagsToShow = tags.take(maxTagsToShow)
    val moreCount = tags.size - maxTagsToShow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
            )
            if (tags.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tagsToShow.forEach { tag ->
                        val formattedTag = tag.joinToString(separator = " ") {
                            "\"${it.jsonPrimitive.content}\""
                        }
                        Text(
                            text = formattedTag,
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                                lineHeight = 20.sp,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (moreCount > 0) {
                        Text(
                            text = stringResource(id = R.string.settings_event_details_tags_more, moreCount),
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                                lineHeight = 20.sp,
                            ),
                        )
                    }
                }
            }
        }
        Icon(
            imageVector = PrimalIcons.Copy,
            contentDescription = stringResource(id = R.string.accessibility_copy_content),
            modifier = Modifier
                .padding(start = 16.dp)
                .clickable { onCopy(tagsAsStringForCopy) },
        )
    }
}

@Composable
private fun Int.toKindTitle(): String {
    return when (this) {
        NostrEventKind.ShortTextNote.value -> stringResource(id = R.string.settings_event_details_kind_publish_note)
        NostrEventKind.Metadata.value -> stringResource(id = R.string.settings_event_details_kind_update_profile)
        NostrEventKind.GenericRepost.value -> stringResource(id = R.string.settings_event_details_kind_repost)
        NostrEventKind.ZapRequest.value -> stringResource(id = R.string.settings_event_details_kind_zap_request)
        NostrEventKind.Zap.value -> stringResource(id = R.string.settings_event_details_kind_zap)
        NostrEventKind.Reaction.value -> stringResource(id = R.string.settings_event_details_kind_react_to_note)
        NostrEventKind.FollowList.value -> stringResource(id = R.string.settings_event_details_kind_update_follow_list)
        NostrEventKind.RelayListMetadata.value -> stringResource(
            id = R.string.settings_event_details_kind_update_relay_list,
        )
        else -> stringResource(id = R.string.settings_event_details_kind_sign_event, this)
    }
}

private fun Int.toKindName(): String {
    return when (this) {
        NostrEventKind.ShortTextNote.value -> "short note"
        NostrEventKind.Metadata.value -> "metadata"
        NostrEventKind.GenericRepost.value -> "generic repost"
        NostrEventKind.ZapRequest.value -> "zap request"
        NostrEventKind.Zap.value -> "zap"
        NostrEventKind.Reaction.value -> "reaction"
        NostrEventKind.FollowList.value -> "contacts"
        NostrEventKind.RelayListMetadata.value -> "relay list"
        else -> "unknown"
    }
}
