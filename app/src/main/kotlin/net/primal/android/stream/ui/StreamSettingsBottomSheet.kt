package net.primal.android.stream.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.theme.AppTheme
import net.primal.domain.streams.StreamContentModerationMode

private val TRACK_SWITCH_COLOR = Color(0xFF2FD058)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamSettingsBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    bottomSheetHeight: Dp?,
    contentModerationMode: StreamContentModerationMode,
    mainHostStreamsMuted: Boolean,
    onContentModerationChanged: (StreamContentModerationMode) -> Unit,
    onStreamNotificationsChanged: (Boolean) -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (bottomSheetHeight != null) Modifier.height(bottomSheetHeight) else Modifier)
                .padding(top = 8.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        ) {
            StreamSettingsToggleColumn(
                value = !mainHostStreamsMuted,
                title = stringResource(id = R.string.live_stream_bottom_sheet_stream_notifications),
                description = stringResource(id = R.string.live_stream_bottom_sheet_stream_notifications_desc),
                onClick = { onStreamNotificationsChanged(!mainHostStreamsMuted) },
            )

            PrimalDivider()

            StreamSettingsToggleColumn(
                value = contentModerationMode == StreamContentModerationMode.Moderated,
                title = stringResource(id = R.string.live_stream_bottom_sheet_chat_filtering),
                description = stringResource(id = R.string.live_stream_bottom_sheet_chat_filtering_desc),
                onClick = { onContentModerationChanged(contentModerationMode) },
            )
        }
    }
}

@Composable
fun StreamSettingsToggleColumn(
    modifier: Modifier = Modifier,
    value: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onPrimary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = description,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            PrimalSwitch(
                modifier = Modifier.padding(start = 32.dp),
                checked = value,
                onCheckedChange = { onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TRACK_SWITCH_COLOR,
                    checkedBorderColor = Color.Transparent,
                    checkedIconColor = Color.Unspecified,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = AppTheme.colorScheme.outline,
                    uncheckedBorderColor = Color.Transparent,
                    uncheckedIconColor = Color.Unspecified,
                ),
            )
        }
    }
}
