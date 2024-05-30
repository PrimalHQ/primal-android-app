package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.acinq.lightning.payment.Bolt11Invoice
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.asFromNowFormat
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.navigation.asUrlDecoded
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun NoteLightningInvoice(
    modifier: Modifier = Modifier,
    invoice: String,
    onPayClick: ((String) -> Unit)? = null,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val clipboardManager = LocalClipboardManager.current

    val bolt11 = remember(invoice) { Bolt11Invoice.read(invoice).get() }
    val amount = numberFormat.format(bolt11.amount?.msat?.div(other = 1_000L))
    val description = bolt11.description.asUrlDecoded()
    val isExpired = bolt11.isExpired(currentTimestampSeconds = Instant.now().epochSecond)
    val expiryIn = bolt11.expirySeconds
    val expireInstant = (bolt11.timestampSeconds + (expiryIn ?: 0)).let(Instant::ofEpochSecond)

    Column(
        modifier = modifier
            .background(
                shape = AppTheme.shapes.medium,
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
            .padding(start = 12.dp, bottom = 12.dp),
    ) {
        InvoiceTitleRow(
            onCopyClick = {
                clipboardManager.setText(AnnotatedString(text = invoice))
            },
        )

        if (description != null) {
            Text(
                text = description,
                style = AppTheme.typography.bodyMedium,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            text = "$amount sats",
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )

        Row(
            modifier = Modifier.defaultMinSize(minHeight = 28.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (isExpired) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.feed_lightning_invoice_expired),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyMedium,
                )
            } else {
                if (expireInstant != null) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(
                            id = R.string.feed_lightning_invoice_expires_in,
                            expireInstant.asFromNowFormat(),
                        ),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
                PrimalLoadingButton(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(end = 12.dp),
                    text = stringResource(id = R.string.feed_lightning_invoice_pay_button),
                    fontSize = 16.sp,
                    onClick = { onPayClick?.invoke(invoice) },
                )
            }
        }
    }
}

@Composable
private fun InvoiceTitleRow(onCopyClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = PrimalIcons.NavWalletBoltFilled,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.zapped,
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f),
            text = stringResource(id = R.string.feed_lightning_invoice_title),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )

        AppBarIcon(
            icon = PrimalIcons.Copy,
            onClick = onCopyClick,
        )
    }
}

@Preview
@Composable
private fun PreviewLightNoteInvoice() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunrise) {
        Surface {
            NoteLightningInvoice(
                invoice = "lnbc888550n1pnp6fz9pp5als09l5nfj9pkqk7mpj6cz6075nd4v95ljz0p65n8zkz03p75t3sdp9wdshgueqv" +
                    "ehhygr3v9q8qunfd4skctnwv46r5cqzzsxqrrs0fppqyyu34ypjxgclynk64hz2r6ddudpaf5mesp5c8mv8xdu67pra9" +
                    "3m3j9aw9mxh08gk09upmjsdpspjxcgcrfjyc0s9qyyssqng6uu0z84h7wlcrlyqywl6jlfd4630k4yd056d3q9h9rg9t" +
                    "zmza5adpzjn489fees4vq0armdskuqgxxvug3et34cqdxj6ldu8lkd2cqcvx5am",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDarkNoteInvoice() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            NoteLightningInvoice(
                invoice = "lnbc888550n1pnp6fz9pp5als09l5nfj9pkqk7mpj6cz6075nd4v95ljz0p65n8zkz03p75t3sdp9wdshgueqv" +
                    "ehhygr3v9q8qunfd4skctnwv46r5cqzzsxqrrs0fppqyyu34ypjxgclynk64hz2r6ddudpaf5mesp5c8mv8xdu67pra9" +
                    "3m3j9aw9mxh08gk09upmjsdpspjxcgcrfjyc0s9qyyssqng6uu0z84h7wlcrlyqywl6jlfd4630k4yd056d3q9h9rg9t" +
                    "zmza5adpzjn489fees4vq0armdskuqgxxvug3et34cqdxj6ldu8lkd2cqcvx5am",
            )
        }
    }
}
