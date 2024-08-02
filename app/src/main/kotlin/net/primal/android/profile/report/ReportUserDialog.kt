package net.primal.android.profile.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun ReportUserDialog(onDismissRequest: () -> Unit, onReportClick: (ReportType) -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            var selectedReportType: ReportType? by remember { mutableStateOf(null) }
            Column(
                modifier = Modifier
                    .background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        shape = AppTheme.shapes.extraLarge,
                    )
                    .padding(vertical = 16.dp, horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.dialog_report_abuse_title),
                    style = AppTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.dialog_report_abuse_description),
                    style = AppTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                ReportType.entries.forEach {
                    RadioTextButton(
                        selected = selectedReportType == it,
                        text = it.name,
                        onClick = {
                            selectedReportType = it
                        },
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(id = R.string.dialog_report_button_dismiss))
                    }

                    TextButton(
                        enabled = selectedReportType != null,
                        onClick = { selectedReportType?.let(onReportClick) },
                    ) {
                        Text(text = stringResource(id = R.string.dialog_report_button_report))
                    }
                }
            }
        },
    )
}

@Composable
private fun RadioTextButton(
    selected: Boolean,
    text: String,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            enabled = enabled,
            onClick = onClick,
        )

        Text(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(top = 4.dp),
            text = text,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewReportUserDialog() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            ReportUserDialog(
                onDismissRequest = {},
                onReportClick = {},
            )
        }
    }
}
