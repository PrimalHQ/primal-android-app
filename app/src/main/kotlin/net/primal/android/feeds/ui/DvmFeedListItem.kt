package net.primal.android.feeds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedLikesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.FeedZapsFilled
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.notes.feed.note.SingleEventStat
import net.primal.android.theme.AppTheme

private val PaidBackground = Color(0xFFFC6337)

@Composable
fun DvmFeedListItem(data: DvmFeed, onFeedClick: ((dvmFeed: DvmFeed) -> Unit)? = null) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFeedClick?.invoke(data) },
        colors = ListItemDefaults.colors(containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2),
        leadingContent = {
            Column {
                AvatarThumbnail(
                    avatarCdnImage = data.avatarUrl?.let { CdnImage(sourceUrl = it) },
                    avatarSize = 40.dp,
                )

                Text(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(width = 40.dp, height = 20.dp)
                        .background(
                            color = if (data.isPaid) {
                                PaidBackground
                            } else {
                                AppTheme.extraColorScheme.onSurfaceVariantAlt2
                            },
                            shape = AppTheme.shapes.extraLarge,
                        ),
                    text = if (data.isPaid) {
                        stringResource(id = R.string.feed_marketplace_paid_feed_label)
                    } else {
                        stringResource(id = R.string.feed_marketplace_free_feed_label)
                    }.uppercase(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (data.isPaid) Color.White else Color.Black,
                    fontSize = 10.sp,
                )
            }
        },
        headlineContent = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = AppTheme.colorScheme.onSurface,
                text = data.title,
            )
        },
        supportingContent = {
            Column {
                if (data.description != null) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        text = data.description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    val numberFormat = NumberFormat.getNumberInstance()

                    SingleEventStat(
                        textCount = numberFormat.format(data.totalLikes ?: 0),
                        highlighted = false,
                        iconVector = PrimalIcons.FeedLikes,
                        iconVectorHighlight = PrimalIcons.FeedLikesFilled,
                        colorHighlight = AppTheme.extraColorScheme.liked,
                        iconContentDescription = stringResource(id = R.string.accessibility_likes_count),
                        iconSize = 18.sp,
                        textStyle = AppTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    SingleEventStat(
                        textCount = numberFormat.format(data.totalSatsZapped ?: 0),
                        highlighted = false,
                        iconVector = PrimalIcons.FeedZaps,
                        iconVectorHighlight = PrimalIcons.FeedZapsFilled,
                        colorHighlight = AppTheme.extraColorScheme.zapped,
                        iconContentDescription = stringResource(id = R.string.accessibility_zaps_count),
                        iconSize = 18.sp,
                        textStyle = AppTheme.typography.bodySmall,
                    )
                }
            }
        },
    )
}
