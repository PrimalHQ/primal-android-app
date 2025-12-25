package net.primal.android.settings.connected.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.settings.connected.model.SessionEventUi
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.RequestState

@Composable
fun SessionEventListItem(
    event: SessionEventUi,
    namingMap: Map<String, String>,
    onClick: () -> Unit,
) {
    val title = namingMap[event.requestTypeId] ?: event.requestTypeId
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = title, style = AppTheme.typography.bodyLarge) },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 5.dp),
                text = buildSessionEventSubtitleString(timestamp = event.timestamp, requestState = event.requestState),
            )
        },
        trailingContent = {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
        colors = ListItemDefaults.colors(containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3),
    )
}

@Composable
private fun buildSessionEventSubtitleString(timestamp: Long, requestState: RequestState): AnnotatedString {
    val formattedTimestamp = rememberPrimalFormattedDateTime(
        timestamp = timestamp,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
    )

    val baseStyle = AppTheme.typography.bodyMedium.toSpanStyle()
    return buildAnnotatedString {
        withStyle(
            style = baseStyle
                .copy(color = AppTheme.extraColorScheme.onSurfaceVariantAlt1),
        ) {
            append(formattedTimestamp)

            if (requestState != RequestState.Pending) {
                withStyle(baseStyle.copy(color = AppTheme.extraColorScheme.onSurfaceVariantAlt3)) {
                    append(" • ")
                }

                when (requestState) {
                    RequestState.Approved -> append(stringResource(id = R.string.session_event_state_approved))
                    RequestState.Rejected -> {
                        withStyle(style = baseStyle.copy(color = AppTheme.extraColorScheme.zapped)) {
                            append(stringResource(id = R.string.session_event_state_rejected))
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
