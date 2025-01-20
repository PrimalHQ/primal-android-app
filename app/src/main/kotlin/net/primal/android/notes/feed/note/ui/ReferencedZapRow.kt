package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.notes.db.ReferencedZap
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.theme.AppTheme

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
            .padding(8.dp)
            .clip(RoundedCornerShape(percent = 100))
            .background(AppTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = referencedZap.senderAvatarCdnImage,
            legendaryCustomization = referencedZap.senderPrimalLegendProfile?.asLegendaryCustomization(),
            onClick = { noteCallbacks.onProfileClick?.invoke(referencedZap.senderId) },
        )

        val numberFormat = NumberFormat.getNumberInstance()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = PrimalIcons.LightningBoltFilled,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppTheme.colorScheme.onPrimary,
                )
                Text(
                    text = numberFormat.format(referencedZap.amountInSats.toLong()),
                    fontWeight = FontWeight.ExtraBold,
                    style = AppTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (referencedZap.message != null && referencedZap.message.isNotEmpty()) {
                Text(
                    text = referencedZap.message,
                    style = AppTheme.typography.bodySmall,
                    maxLines = 1,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        UniversalAvatarThumbnail(
            avatarCdnImage = referencedZap.receiverAvatarCdnImage,
            legendaryCustomization = referencedZap.receiverPrimalLegendProfile?.asLegendaryCustomization(),
            onClick = { noteCallbacks.onProfileClick?.invoke(referencedZap.receiverId) },
        )
    }
}
