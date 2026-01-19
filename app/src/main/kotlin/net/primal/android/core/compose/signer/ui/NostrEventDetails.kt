package net.primal.android.core.compose.signer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.CopyAlt
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.theme.CourierPrimeFontFamily

private const val CONTENT_MAX_LINES_COLLAPSED = 10
private const val MAX_TAGS_TO_SHOW_IN_DETAILS = 10

sealed interface EventDetailRow {
    data class Detail(
        val label: String,
        val value: String,
        val singleLine: Boolean = true,
        val expandable: Boolean = false,
        val isKey: Boolean = false,
        val maxLines: Int = CONTENT_MAX_LINES_COLLAPSED,
    ) : EventDetailRow

    data class Tags(
        val label: String,
        val tags: List<JsonArray>,
    ) : EventDetailRow
}

@Composable
fun NostrEventDetails(
    title: String,
    subtitle: String,
    rows: List<EventDetailRow>,
    modifier: Modifier = Modifier,
    status: String? = null,
    statusColor: Color? = null,
    onCopy: (text: String, label: String) -> Unit,
    footerContent: (@Composable () -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        item(key = "Header", contentType = "Header") {
            EventDetailsHeader(
                title = title,
                subtitle = subtitle,
                status = status,
                statusColor = statusColor,
            )
        }

        itemsIndexed(
            items = rows,
            key = { index, _ -> index },
        ) { index, item ->
            val shape = getListItemShape(index = index, listSize = rows.size)
            val isLast = index == rows.lastIndex

            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
            ) {
                when (item) {
                    is EventDetailRow.Detail -> EventDetailListItem(
                        item = item,
                        onCopy = { onCopy(item.value, item.label) },
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

        if (footerContent != null) {
            item(key = "Footer", contentType = "Button") {
                footerContent()
            }
        }
    }
}

@Composable
private fun EventDetailsHeader(
    title: String,
    subtitle: String,
    status: String?,
    statusColor: Color?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        Text(
            text = subtitle,
            style = AppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )

        if (!status.isNullOrBlank() && statusColor != null) {
            Text(
                text = status,
                style = AppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = statusColor,
            )
        }
    }
}

@Composable
private fun EventDetailListItem(item: EventDetailRow.Detail, onCopy: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var showToggle by remember { mutableStateOf(false) }

    val displayValue = remember(item.value, item.isKey) {
        if (item.isKey) item.value.ellipsizeMiddle(size = 12) else item.value
    }

    val maxLines = if (item.singleLine) {
        1
    } else if (item.expandable && !isExpanded) {
        item.maxLines
    } else {
        Int.MAX_VALUE
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
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
                    if (item.expandable) {
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
