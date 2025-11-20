package net.primal.android.core.compose.nostr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.CopyAlt
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.theme.AppTheme
import net.primal.android.theme.CourierPrimeFontFamily
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

private const val CONTENT_MAX_LINES_COLLAPSED = 10
private const val MAX_TAGS_TO_SHOW_IN_DETAILS = 10

private sealed interface EventDetailRow {
    data class Detail(
        val label: String,
        val value: String,
        val singleLine: Boolean = true,
        val expandable: Boolean = false,
        val isKey: Boolean = false,
    ) : EventDetailRow

    data class Tags(
        val label: String,
        val tags: List<JsonArray>,
    ) : EventDetailRow
}

@Composable
fun NostrEventDetails(
    event: NostrEvent,
    modifier: Modifier = Modifier,
    onCopy: (text: String, label: String) -> Unit,
    rawJson: String? = null,
    footerContent: (@Composable () -> Unit)? = null,
) {
    val eventDetailRows = buildEventDetailRows(event = event)

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        item(key = "Header", contentType = "Header") {
            EventDetailsHeader(
                eventKind = event.kind,
                timestamp = event.createdAt,
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
            val shape = getListItemShape(index = index, listSize = eventDetailRows.size)
            val isLast = index == eventDetailRows.lastIndex

            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
            ) {
                when (item) {
                    is EventDetailRow.Detail -> EventDetailListItem(
                        label = item.label,
                        value = item.value,
                        onCopy = { onCopy(item.value, item.label) },
                        singleLine = item.singleLine,
                        expandable = item.expandable,
                        isKey = item.isKey,
                    )

                    is EventDetailRow.Tags -> TagsListItem(
                        label = item.label,
                        tags = item.tags,
                        onCopy = { textToCopy -> onCopy(textToCopy, item.label) },
                    )
                }
                if (!isLast) {
                    PrimalDivider()
                }
            }
        }

        item(key = "Footer", contentType = "Button") {
            if (footerContent != null) {
                footerContent()
            } else if (rawJson != null) {
                val rawJsonLabel = stringResource(id = R.string.settings_event_details_raw_json_label)
                PrimalFilledButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(50.dp),
                    onClick = { onCopy(rawJson, rawJsonLabel) },
                ) {
                    Text(text = stringResource(id = R.string.settings_event_details_copy_raw_json))
                }
            }
        }
    }
}

@Composable
private fun buildEventDetailRows(event: NostrEvent): List<EventDetailRow> {
    return buildList {
        if (event.id.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = stringResource(id = R.string.settings_event_details_id_label),
                    value = event.id,
                    isKey = true,
                ),
            )
        }
        add(
            EventDetailRow.Detail(
                label = stringResource(id = R.string.settings_event_details_pubkey_label),
                value = event.pubKey,
                isKey = true,
            ),
        )
        add(
            EventDetailRow.Detail(
                label = stringResource(id = R.string.settings_event_details_event_kind_label),
                value = "${event.kind} - ${event.kind.toKindName()}",
            ),
        )
        add(
            EventDetailRow.Detail(
                label = stringResource(id = R.string.settings_event_details_created_at_label),
                value = event.createdAt.toString(),
            ),
        )
        if (event.tags.isNotEmpty()) {
            add(
                EventDetailRow.Tags(
                    label = stringResource(id = R.string.settings_event_details_tags_label),
                    tags = event.tags,
                ),
            )
        }
        if (event.content.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = stringResource(id = R.string.settings_event_details_content_label),
                    value = event.content,
                    singleLine = false,
                    expandable = true,
                ),
            )
        }
        if (event.sig.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = stringResource(id = R.string.settings_event_details_signature_label),
                    value = event.sig,
                    isKey = true,
                ),
            )
        }
    }
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
            style = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = formattedTimestamp,
            style = AppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
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
    isKey: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showToggle by remember { mutableStateOf(false) }

    val displayValue = remember(value, isKey) {
        if (isKey) value.ellipsizeMiddle(size = 12) else value
    }

    val maxLines = if (singleLine) 1 else if (expandable && !isExpanded) CONTENT_MAX_LINES_COLLAPSED else Int.MAX_VALUE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = displayValue,
                style = AppTheme.typography.bodyMedium.copy(
                    fontFamily = CourierPrimeFontFamily,
                    lineHeight = 24.sp,
                ),
                maxLines = maxLines,
                overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
                onTextLayout = {
                    if (expandable) {
                        showToggle = it.didOverflowHeight || isExpanded
                    }
                },
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                fontWeight = FontWeight.Bold,
            )

            AnimatedVisibility(visible = showToggle) {
                Text(
                    text = if (isExpanded) {
                        stringResource(id = R.string.settings_event_details_show_less)
                    } else {
                        stringResource(id = R.string.settings_event_details_show_more)
                    },
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colorScheme.primary,
                        fontFamily = CourierPrimeFontFamily,
                    ),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { isExpanded = !isExpanded },
                )
            }
        }
        Icon(
            modifier = Modifier
                .size(16.dp)
                .clickable { onCopy() },
            imageVector = PrimalIcons.CopyAlt,
            contentDescription = stringResource(id = R.string.accessibility_copy_content),
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
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

    val tagsToShow = tags.take(MAX_TAGS_TO_SHOW_IN_DETAILS)
    val moreCount = tags.size - MAX_TAGS_TO_SHOW_IN_DETAILS

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
                style = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onSurface,
            )
            if (tags.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tagsToShow.forEach { tag ->
                        val formattedTag = tag.joinToString(separator = ", ") {
                            "\"${it.jsonPrimitive.content}\""
                        }
                        Text(
                            text = formattedTag,
                            style = AppTheme.typography.bodyMedium.copy(
                                fontFamily = CourierPrimeFontFamily,
                                lineHeight = 24.sp,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (moreCount > 0) {
                        Text(
                            text = stringResource(id = R.string.settings_event_details_tags_more, moreCount),
                            style = AppTheme.typography.bodyMedium.copy(
                                lineHeight = 24.sp,
                                fontFamily = CourierPrimeFontFamily,
                            ),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        Icon(
            modifier = Modifier
                .size(16.dp)
                .clickable { onCopy(tagsAsStringForCopy) },
            imageVector = PrimalIcons.CopyAlt,
            contentDescription = stringResource(id = R.string.accessibility_copy_content),
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
    }
}

@Composable
fun Int.toKindTitle(): String {
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
