package net.primal.android.stream.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PulsingListItemPlaceholder
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.theme.AppTheme

private val ZAPS_SECTION_HEIGHT = 64.dp
private const val OTHER_ZAPS_COUNT = 3

@Composable
fun StreamTopZapsSection(
    modifier: Modifier = Modifier,
    chatLoading: Boolean,
    topZaps: List<EventZapUiModel>,
    onZapClick: () -> Unit,
    onTopZapsClick: () -> Unit,
) {
    when {
        chatLoading -> LoadingTopZapsSection(modifier)

        topZaps.isEmpty() -> EmptyTopZapsSection(modifier, onZapClick)

        else -> TopZapsSection(modifier, topZaps, onTopZapsClick, onZapClick)
    }
}

@Composable
private fun LoadingTopZapsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(ZAPS_SECTION_HEIGHT),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PulsingListItemPlaceholder(height = 26.dp, shape = CircleShape, widthFraction = 0.6f)

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(times = 4) {
                PulsingListItemPlaceholder(
                    modifier = Modifier.weight(1f),
                    height = 26.dp,
                    shape = CircleShape,
                )
            }
        }
    }
}

@Composable
private fun TopZapsSection(
    modifier: Modifier,
    topZaps: List<EventZapUiModel>,
    onTopZapsClick: () -> Unit,
    onZapClick: () -> Unit,
) {
    val topZap = topZaps.first()
    val otherZaps = topZaps.drop(n = 1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(ZAPS_SECTION_HEIGHT),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StreamTopNoteZapRow(
            noteZap = topZap,
            onClick = onTopZapsClick,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (otherZaps.isEmpty()) Arrangement.End else Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (otherZaps.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    maxItemsInEachRow = OTHER_ZAPS_COUNT,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    otherZaps.take(OTHER_ZAPS_COUNT).forEach {
                        key(it.id) {
                            StreamNoteZapListItem(
                                noteZap = it,
                                onClick = onTopZapsClick,
                            )
                        }
                    }
                }
            }

            ZapButton(onClick = onZapClick)
        }
    }
}

@Composable
private fun EmptyTopZapsSection(modifier: Modifier, onZapClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ZAPS_SECTION_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = AppTheme.colorScheme.onSurface,
                    shape = AppTheme.shapes.extraLarge,
                )
                .clickable { onZapClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconText(
                text = stringResource(id = R.string.stream_be_the_first_to_zap),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                ),
                color = AppTheme.colorScheme.surface,
                leadingIcon = PrimalIcons.NavWalletBoltFilled,
                iconSize = 16.sp,
            )
        }
    }
}

@Composable
private fun StreamTopNoteZapRow(noteZap: EventZapUiModel, onClick: () -> Unit) {
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
private fun StreamNoteZapListItem(noteZap: EventZapUiModel, onClick: () -> Unit) {
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
private fun ZapButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(26.dp)
            .padding(horizontal = 6.dp)
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
