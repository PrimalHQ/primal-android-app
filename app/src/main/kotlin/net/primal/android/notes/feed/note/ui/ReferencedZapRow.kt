package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.notes.db.ReferencedZap
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ReferencedZapRow(
    modifier: Modifier = Modifier,
    referencedZap: ReferencedZap,
    noteCallbacks: NoteCallbacks,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 100))
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(percent = 100))
                .background(AppTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UniversalAvatarThumbnail(
                avatarSize = 36.dp,
                avatarCdnImage = referencedZap.senderAvatarCdnImage,
                legendaryCustomization = referencedZap.senderPrimalLegendProfile?.asLegendaryCustomization(),
                onClick = { noteCallbacks.onProfileClick?.invoke(referencedZap.senderId) },
            )

            ZapAmountAndMessageColumn(
                amountInSats = referencedZap.amountInSats,
                message = referencedZap.message,
            )

            UniversalAvatarThumbnail(
                avatarSize = 36.dp,
                avatarCdnImage = referencedZap.receiverAvatarCdnImage,
                legendaryCustomization = referencedZap.receiverPrimalLegendProfile?.asLegendaryCustomization(),
                onClick = { noteCallbacks.onProfileClick?.invoke(referencedZap.receiverId) },
            )
        }

        if (referencedZap.receiverDisplayName != null) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = referencedZap.receiverDisplayName,
                style = AppTheme.typography.bodySmall,
                fontSize = 14.sp,
                maxLines = 1,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ZapAmountAndMessageColumn(amountInSats: Double, message: String?) {
    val numberFormat = NumberFormat.getNumberInstance()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = PrimalIcons.LightningBoltFilled,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = AppTheme.colorScheme.onPrimary,
            )
            Text(
                text = numberFormat.format(amountInSats.toLong()),
                fontWeight = FontWeight.ExtraBold,
                style = AppTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.colorScheme.onPrimary,
            )
        }
        if (message != null && message.isNotEmpty()) {
            Text(
                text = message,
                style = AppTheme.typography.bodySmall,
                fontSize = 12.sp,
                maxLines = 1,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
fun PreviewMessageAndDisplayName() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ReferencedZapRow(
            modifier = Modifier.width(300.dp),
            noteCallbacks = NoteCallbacks(),
            referencedZap = ReferencedZap(
                senderId = "",
                senderAvatarCdnImage = null,
                senderPrimalLegendProfile = null,
                receiverId = "",
                receiverDisplayName = "qauser",
                receiverAvatarCdnImage = null,
                receiverPrimalLegendProfile = null,
                zappedEventId = null,
                amountInSats = 1000.0,
                message = "Onwards!",
            ),
        )
    }
}

@Preview
@Composable
fun PreviewNoMessageAndDisplayName() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ReferencedZapRow(
            modifier = Modifier.width(300.dp),
            noteCallbacks = NoteCallbacks(),
            referencedZap = ReferencedZap(
                senderId = "",
                senderAvatarCdnImage = null,
                senderPrimalLegendProfile = null,
                receiverId = "",
                receiverDisplayName = "qauser",
                receiverAvatarCdnImage = null,
                receiverPrimalLegendProfile = null,
                zappedEventId = null,
                amountInSats = 1000.0,
                message = null,
            ),
        )
    }
}

@Preview
@Composable
fun PreviewNoMessageAndNoDisplayName() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ReferencedZapRow(
            modifier = Modifier.width(300.dp),
            noteCallbacks = NoteCallbacks(),
            referencedZap = ReferencedZap(
                senderId = "",
                senderAvatarCdnImage = null,
                senderPrimalLegendProfile = null,
                receiverId = "",
                receiverDisplayName = null,
                receiverAvatarCdnImage = null,
                receiverPrimalLegendProfile = null,
                zappedEventId = null,
                amountInSats = 1000.0,
                message = null,
            ),
        )
    }
}

@Preview
@Composable
fun PreviewMessageAndNoDisplayName() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ReferencedZapRow(
            modifier = Modifier.width(300.dp),
            noteCallbacks = NoteCallbacks(),
            referencedZap = ReferencedZap(
                senderId = "",
                senderAvatarCdnImage = null,
                senderPrimalLegendProfile = null,
                receiverId = "",
                receiverDisplayName = null,
                receiverAvatarCdnImage = null,
                receiverPrimalLegendProfile = null,
                zappedEventId = null,
                amountInSats = 1000.0,
                message = "Onwards!",
            ),
        )
    }
}
