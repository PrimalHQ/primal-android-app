package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletError
import net.primal.android.theme.AppTheme

private const val SHARE_LOGS_ANNOTATION_TAG = "ShareLogsTag"

@Composable
fun UpgradeWalletFailed(
    modifier: Modifier,
    errorMessage: String,
    errorLogs: List<String>,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    onShareLogsClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 80.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FailedStatusColumn(
            headlineText = stringResource(id = R.string.wallet_upgrade_failed_headline),
            errorMessage = errorMessage,
            showShareLogs = errorLogs.isNotEmpty(),
            onShareLogsClick = onShareLogsClick,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PrimalLoadingButton(
                modifier = Modifier.width(200.dp),
                text = stringResource(id = R.string.wallet_upgrade_retry_button),
                onClick = onRetryClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCloseClick) {
                Text(
                    text = stringResource(id = R.string.wallet_upgrade_close_button),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun FailedStatusColumn(
    headlineText: String,
    errorMessage: String,
    showShareLogs: Boolean,
    onShareLogsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        androidx.compose.foundation.Image(
            modifier = Modifier
                .height(160.dp)
                .padding(vertical = 16.dp),
            imageVector = PrimalIcons.WalletError,
            contentDescription = null,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(color = AppTheme.colorScheme.error),
        )

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 32.dp, bottom = 8.dp),
            text = headlineText,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.headlineSmall,
        )

        if (showShareLogs) {
            ErrorMessageWithShareLogs(
                errorMessage = errorMessage,
                onShareLogsClick = onShareLogsClick,
            )
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(vertical = 32.dp),
                text = errorMessage,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
            )
        }
    }
}

@Composable
private fun ErrorMessageWithShareLogs(errorMessage: String, onShareLogsClick: () -> Unit) {
    val shareLogsText = stringResource(id = R.string.wallet_upgrade_share_logs)
    val linkSpanStyle = SpanStyle(
        color = AppTheme.colorScheme.secondary,
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.SemiBold,
    )

    val annotatedString = buildAnnotatedString {
        append(errorMessage)
        append(" ")
        pushStringAnnotation(SHARE_LOGS_ANNOTATION_TAG, "share")
        withStyle(style = linkSpanStyle) {
            append(shareLogsText)
        }
        pop()
    }

    PrimalClickableText(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.8f)
            .padding(vertical = 32.dp),
        text = annotatedString,
        style = AppTheme.typography.bodyLarge.copy(
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        ),
        onClick = { position, _ ->
            annotatedString.getStringAnnotations(
                start = position,
                end = position,
            ).firstOrNull()?.let { annotation ->
                if (annotation.tag == SHARE_LOGS_ANNOTATION_TAG) {
                    onShareLogsClick()
                }
            }
        },
    )
}
