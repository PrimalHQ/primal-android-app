package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextReportContent
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsSection(message: ChatMessageUi, onReport: (ReportType) -> Unit) {
    var reportDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_chat_message),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 16.sp,
            ),
            color = if (isAppInDarkPrimalTheme()) {
                AppTheme.extraColorScheme.onSurfaceVariantAlt4
            } else {
                AppTheme.extraColorScheme.onSurfaceVariantAlt2
            },
        )

        ChatMessageListItem(message = message, onProfileClick = {}, onNostrUriClick = {})

        PrimalFilledButton(
            modifier = Modifier.padding(start = 40.dp),
            containerColor = ReportButtonHandleColor,
            contentColor = AppTheme.colorScheme.onSurface,
            textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
            onClick = { reportDialogVisible = true },
            contentPadding = PaddingValues(18.dp, vertical = 1.dp),
            height = 37.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = PrimalIcons.ContextReportContent,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(3.dp),
                    text = stringResource(id = R.string.live_stream_report_message_button),
                )
            }
        }
    }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReport(type)
            },
        )
    }
}
