package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletUpgradeError
import net.primal.android.theme.AppTheme

private const val COPY_LOGS_ANNOTATION_TAG = "CopyLogsTag"

@Composable
fun UpgradeWalletFailed(
    modifier: Modifier,
    onRetryClick: () -> Unit,
    onCopyLogsClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 32.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                modifier = Modifier.size(160.dp),
                imageVector = PrimalIcons.WalletUpgradeError,
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                ),
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                text = stringResource(id = R.string.wallet_upgrade_failed_headline),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                ),
            )

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = stringResource(id = R.string.wallet_upgrade_failed_subtitle_1),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.wallet_upgrade_failed_subtitle_2),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SupportMessageWithCopyLogs(
                onCopyLogsClick = onCopyLogsClick,
            )
        }

        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(id = R.string.wallet_upgrade_try_again_button),
            onClick = onRetryClick,
        )
    }
}

@Composable
private fun SupportMessageWithCopyLogs(onCopyLogsClick: () -> Unit) {
    val support1 = stringResource(id = R.string.wallet_upgrade_failed_support_1)
    val email = stringResource(id = R.string.wallet_upgrade_failed_support_email)
    val support2 = stringResource(id = R.string.wallet_upgrade_failed_support_2)
    val copyLog = stringResource(id = R.string.wallet_upgrade_failed_support_copy_log)
    val support3 = stringResource(id = R.string.wallet_upgrade_failed_support_3)

    val boldSpanStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
    )

    val linkSpanStyle = SpanStyle(
        color = AppTheme.colorScheme.secondary,
    )

    val annotatedString = buildAnnotatedString {
        append("$support1 ")
        withStyle(style = boldSpanStyle) {
            append(email)
        }
        append(support2)
        append(" ")
        pushStringAnnotation(COPY_LOGS_ANNOTATION_TAG, "copy")
        withStyle(style = linkSpanStyle) {
            append(copyLog)
        }
        pop()
        append(support3)
    }

    PrimalClickableText(
        modifier = Modifier.fillMaxWidth(),
        text = annotatedString,
        style = AppTheme.typography.bodyLarge.copy(
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        ),
        onClick = { position, _ ->
            annotatedString.getStringAnnotations(
                start = position,
                end = position,
            ).firstOrNull()?.let { annotation ->
                if (annotation.tag == COPY_LOGS_ANNOTATION_TAG) {
                    onCopyLogsClick()
                }
            }
        },
    )
}
