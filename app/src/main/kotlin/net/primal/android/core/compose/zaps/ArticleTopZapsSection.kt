package net.primal.android.core.compose.zaps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.stats.ui.EventZapUiModel
import net.primal.android.theme.AppTheme

@Composable
fun ArticleTopZapsSection(
    modifier: Modifier,
    topZaps: List<EventZapUiModel>,
    onZapClick: () -> Unit,
    onTopZapsClick: () -> Unit,
) {
    val topZap = topZaps.firstOrNull()
    val otherZaps = topZaps.drop(n = 1)
    Column(
        modifier = modifier.animateContentSize(),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        if (topZap != null) {
            ArticleTopNoteZapRow(
                noteZap = topZap,
                onClick = onTopZapsClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (otherZaps.isNotEmpty()) {
            Row {
                otherZaps.take(n = 3).forEach {
                    key(it.id) {
                        ArticleNoteZapListItem(
                            noteZap = it,
                            onClick = onTopZapsClick,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
                ZapButton(onClick = onZapClick)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ArticleTopNoteZapRow(noteZap: EventZapUiModel, onClick: () -> Unit) {
    val numberFormat = NumberFormat.getNumberInstance()
    Row(
        modifier = Modifier
            .height(30.dp)
            .animateContentSize()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UniversalAvatarThumbnail(
            modifier = Modifier.padding(start = 2.dp),
            avatarCdnImage = noteZap.zapperAvatarCdnImage,
            avatarSize = 28.dp,
            onClick = onClick,
            legendaryCustomization = noteZap.zapperLegendaryCustomization,
        )

        IconText(
            modifier = Modifier
                .padding(start = 6.dp, end = 8.dp)
                .padding(top = 1.dp),
            text = numberFormat.format(noteZap.amountInSats.toLong()),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            leadingIcon = PrimalIcons.NavWalletBoltFilled,
            iconSize = 16.sp,
        )

        if (!noteZap.message.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(end = 16.dp, top = 1.dp),
                text = noteZap.message,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ArticleNoteZapListItem(noteZap: EventZapUiModel, onClick: () -> Unit) {
    val numberFormat = NumberFormat.getNumberInstance()
    Row(
        modifier = Modifier
            .height(26.dp)
            .animateContentSize()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UniversalAvatarThumbnail(
            modifier = Modifier.padding(start = 2.dp),
            avatarCdnImage = noteZap.zapperAvatarCdnImage,
            avatarSize = 24.dp,
            onClick = onClick,
            legendaryCustomization = noteZap.zapperLegendaryCustomization,
        )

        Text(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 2.dp),
            text = numberFormat.format(noteZap.amountInSats.toLong()),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
            ),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun ZapButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(26.dp)
            .animateContentSize()
            .background(
                color = AppTheme.colorScheme.onSurface,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconText(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(end = 2.dp),
            text = stringResource(id = R.string.article_details_zap),
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
            ),
            leadingIcon = PrimalIcons.NavWalletBoltFilled,
            iconSize = 16.sp,
            color = AppTheme.colorScheme.surface,
        )
    }
}

@Preview
@Composable
private fun PreviewArticleTopZapsSection() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            ArticleTopZapsSection(
                modifier = Modifier.fillMaxWidth(),
                topZaps = listOf(
                    EventZapUiModel(
                        id = "id",
                        zapperId = "id",
                        zapperName = "topZapper",
                        zapperHandle = "handle",
                        zappedAt = 0,
                        message = "Top zap message!!!",
                        amountInSats = 21_21_21.toULong(),
                        zapperLegendaryCustomization = LegendaryCustomization(
                            avatarGlow = true,
                            customBadge = true,
                            legendaryStyle = LegendaryStyle.SUN_FIRE,
                        )
                    ),
                    EventZapUiModel(
                        id = "id",
                        zapperId = "id",
                        zapperName = "topZapper",
                        zapperHandle = "handle",
                        zappedAt = 0,
                        message = "",
                        amountInSats = 20_000.toULong(),
                    ),
                    EventZapUiModel(
                        id = "id",
                        zapperId = "id",
                        zapperName = "topZapper",
                        zapperHandle = "handle",
                        zappedAt = 0,
                        message = "",
                        amountInSats = 10_000.toULong(),
                    ),
                    EventZapUiModel(
                        id = "id",
                        zapperId = "id",
                        zapperName = "topZapper",
                        zapperHandle = "handle",
                        zappedAt = 0,
                        message = "",
                        amountInSats = 8_888.toULong(),
                    ),
                    EventZapUiModel(
                        id = "id",
                        zapperId = "id",
                        zapperName = "topZapper",
                        zapperHandle = "handle",
                        zappedAt = 0,
                        message = "",
                        amountInSats = 2048.toULong(),
                    ),
                    EventZapUiModel(
                        id = "id",
                        zapperId = "id",
                        zapperName = "topZapper",
                        zapperHandle = "handle",
                        zappedAt = 0,
                        message = "",
                        amountInSats = 1028.toULong(),
                    ),
                ),
                onZapClick = {},
                onTopZapsClick = {},
            )
        }
    }
}
