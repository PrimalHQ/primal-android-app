package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NavWalletFilled
import net.primal.android.theme.AppTheme

@Composable
fun UpgradeWalletReady(
    modifier: Modifier = Modifier,
    walletBalanceInSats: Long?,
    onStartUpgrade: () -> Unit,
    onFaqClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 52.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier
                    .size(130.dp),
                imageVector = PrimalIcons.NavWalletFilled,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )

            Spacer(modifier = Modifier.height(75.dp))

            BulletList(walletBalanceInSats = walletBalanceInSats)

            Spacer(modifier = Modifier.height(19.dp))

            Text(
                text = stringResource(id = R.string.wallet_upgrade_keep_open),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = AppTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(65.dp))

            FaqLink(onFaqClick = onFaqClick)

            Spacer(modifier = Modifier.weight(1f))
        }

        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(id = R.string.wallet_upgrade_button_now),
            onClick = onStartUpgrade,
        )
    }
}

@Composable
private fun BulletList(walletBalanceInSats: Long?) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        BulletItem(text = stringResource(id = R.string.wallet_upgrade_ready_bullet_1))

        val formattedSats = walletBalanceInSats
            ?.let { numberFormat.format(it) } ?: "-"
        val balanceText = buildAnnotatedString {
            append(stringResource(id = R.string.wallet_upgrade_ready_bullet_2_pre))
            append(" ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AppTheme.colorScheme.onSurface)) {
                append("$formattedSats ${stringResource(id = R.string.wallet_sats_suffix)}")
            }
            append(" ")
            append(stringResource(id = R.string.wallet_upgrade_ready_bullet_2_post))
        }
        BulletItem(text = balanceText)

        BulletItem(text = stringResource(id = R.string.wallet_upgrade_ready_bullet_3))
        BulletItem(text = stringResource(id = R.string.wallet_upgrade_ready_bullet_4))
    }
}

@Composable
private fun BulletItem(text: CharSequence) {
    val bulletColor = AppTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Canvas(
            modifier = Modifier
                .padding(top = 8.dp, end = 12.dp)
                .size(8.dp),
        ) {
            drawCircle(color = bulletColor)
        }

        when (text) {
            is String -> Text(
                text = text,
                style = AppTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = AppTheme.colorScheme.onSurface,
            )
            is AnnotatedString -> Text(
                text = text,
                style = AppTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = AppTheme.colorScheme.onSurface,
            )
        }
    }
}

private const val FAQ_ANNOTATION_TAG = "FaqTag"

@Composable
private fun FaqLink(onFaqClick: () -> Unit) {
    val faqText = stringResource(id = R.string.wallet_upgrade_faq_question)
    val faqLink = stringResource(id = R.string.wallet_upgrade_faq_link)

    val textColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = textColor)) {
            append(faqText)
        }
        append(" ")
        pushStringAnnotation(tag = FAQ_ANNOTATION_TAG, annotation = "faq")
        withStyle(
            style = SpanStyle(
                color = AppTheme.colorScheme.secondary,
            ),
        ) {
            append(faqLink)
        }
        pop()
        withStyle(style = SpanStyle(color = textColor)) {
            append(".")
        }
    }

    PrimalClickableText(
        modifier = Modifier.padding(bottom = 24.dp),
        text = annotatedString,
        style = AppTheme.typography.bodyLarge.copy(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        ),
        onClick = { position, _ ->
            annotatedString.getStringAnnotations(
                start = position,
                end = position,
            ).firstOrNull()?.let { annotation ->
                if (annotation.tag == FAQ_ANNOTATION_TAG) {
                    onFaqClick()
                }
            }
        },
    )
}
