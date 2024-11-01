package net.primal.android.core.compose.zaps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZapFilled
import net.primal.android.stats.ui.EventZapUiModel
import net.primal.android.theme.AppTheme

@Composable
fun EventZapItem(
    noteZap: EventZapUiModel,
    modifier: Modifier = Modifier,
    showMessage: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val numberFormat = NumberFormat.getNumberInstance()
    Row(
        modifier = modifier
            .height(26.dp)
            .animateContentSize()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            modifier = Modifier.padding(start = 2.dp),
            avatarCdnImage = noteZap.zapperAvatarCdnImage,
            avatarSize = 24.dp,
            onClick = onClick,
        )

        IconText(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
            text = numberFormat.format(noteZap.amountInSats.toLong()),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            leadingIcon = if (showMessage) PrimalIcons.FeedNewZapFilled else null,
            iconSize = 14.sp,
        )

        if (showMessage && !noteZap.message.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = noteZap.message,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}
