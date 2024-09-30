package net.primal.android.feeds.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import net.primal.android.notes.feed.note.ui.SingleEventStat
import net.primal.android.theme.AppTheme

private val PaidBackground = Color(0xFFFC6337)
private val FreeTextColor = Color(0xFF111111)

@Composable
fun DvmFeedListItem(
    modifier: Modifier = Modifier,
    data: DvmFeed,
    onFeedClick: ((dvmFeed: DvmFeed) -> Unit)? = null,
    listItemContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt2,
    avatarSize: Dp = 48.dp,
    extended: Boolean = false,
) {
    val viewModel = hiltViewModel<DvmFeedListItemViewModel, DvmFeedListItemViewModel.Factory>(
        key = data.dvmId,
        creationCallback = { factory -> factory.create(dvmFeed = data) },
    )

    val uiState = viewModel.state.collectAsState()

    DvmFeedListItem(
        modifier = modifier,
        state = uiState.value,
        onFeedClick = onFeedClick,
        eventPublisher = viewModel::setEvent,
        listItemContainerColor = listItemContainerColor,
        avatarSize = avatarSize,
        extended = extended,
    )
}

@Composable
private fun DvmFeedListItem(
    modifier: Modifier = Modifier,
    state: DvmFeedListItemContract.UiState,
    onFeedClick: ((dvmFeed: DvmFeed) -> Unit)? = null,
    eventPublisher: (DvmFeedListItemContract.UiEvent) -> Unit,
    listItemContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt2,
    avatarSize: Dp = 48.dp,
    extended: Boolean = false,
) {
    Column(
        modifier = Modifier
            .clip(AppTheme.shapes.small)
            .background(listItemContainerColor)
            .clickable { onFeedClick?.invoke(state.dvmFeed) },
    ) {
        ListItem(
            modifier = modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(containerColor = listItemContainerColor),
            leadingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AvatarThumbnail(
                        avatarCdnImage = state.dvmFeed.avatarUrl?.let { CdnImage(sourceUrl = it) },
                        avatarSize = avatarSize,
                    )
                    when {
                        state.dvmFeed.primalSubscriptionRequired == true -> SubBadge(width = avatarSize)
                        state.dvmFeed.isPaid -> PaidBadge(width = avatarSize)
                        else -> FreeBadge(width = avatarSize)
                    }
                }
            },
            headlineContent = {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colorScheme.onSurface,
                    text = state.dvmFeed.title,
                    maxLines = if (extended) 2 else 1,
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    if (state.dvmFeed.description != null) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            text = state.dvmFeed.description,
                            maxLines = if (extended) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (extended) {
                        CreatedByPrimalRow()
                    }
                    DvmFeedActionRow(
                        userLiked = state.dvmFeed.userLiked ?: false,
                        userZapped = state.dvmFeed.userZapped ?: false,
                        totalLikes = state.dvmFeed.totalLikes,
                        totalSatsZapped = state.dvmFeed.totalSatsZapped,
                        onLikeClick = { eventPublisher(DvmFeedListItemContract.UiEvent.OnLikeClick) },
                        onZapClick = { eventPublisher(DvmFeedListItemContract.UiEvent.OnZapClick) },
                    )
                }
            },
        )
    }
}

@Composable
private fun CreatedByPrimalRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.primal_wave_logo_summer),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    ),
                ) {
                    append("Created by")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" Primal")
                }
            },
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DvmFeedActionRow(
    userLiked: Boolean,
    userZapped: Boolean,
    totalLikes: Long?,
    totalSatsZapped: Long?,
    onLikeClick: () -> Unit,
    onZapClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val numberFormat = NumberFormat.getNumberInstance()

        SingleEventStat(
            textCount = numberFormat.format(totalLikes ?: 0),
            highlighted = userLiked,
            iconVector = PrimalIcons.FeedLikes,
            iconVectorHighlight = PrimalIcons.FeedLikesFilled,
            colorHighlight = AppTheme.extraColorScheme.liked,
            iconContentDescription = stringResource(id = R.string.accessibility_likes_count),
            iconSize = 18.sp,
            textStyle = AppTheme.typography.bodySmall,
            onClick = onLikeClick,
        )

        SingleEventStat(
            textCount = numberFormat.format(totalSatsZapped ?: 0),
            highlighted = userZapped,
            iconVector = PrimalIcons.FeedZaps,
            iconVectorHighlight = PrimalIcons.FeedZapsFilled,
            colorHighlight = AppTheme.extraColorScheme.zapped,
            iconContentDescription = stringResource(id = R.string.accessibility_zaps_count),
            iconSize = 18.sp,
            textStyle = AppTheme.typography.bodySmall,
            onClick = onZapClick,
        )
    }
}

@Composable
private fun SubBadge(width: Dp) {
    Badge(
        text = "SUB",
        backgroundColor = AppTheme.colorScheme.tertiary,
        textColor = AppTheme.colorScheme.onPrimary,
        width = width,
    )
}

@Composable
private fun PaidBadge(width: Dp) {
    Badge(
        text = "PAID",
        backgroundColor = PaidBackground,
        textColor = AppTheme.colorScheme.onPrimary,
        width = width,
    )
}

@Composable
private fun FreeBadge(width: Dp) {
    Badge(
        text = "FREE",
        backgroundColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        textColor = FreeTextColor,
        width = width,
    )
}

@Composable
private fun Badge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    width: Dp,
) {
    Text(
        modifier = Modifier
            .width(width)
            .clip(AppTheme.shapes.extraLarge)
            .background(backgroundColor)
            .padding(vertical = 5.dp),
        text = text,
        fontWeight = FontWeight.Bold,
        color = textColor,
        style = AppTheme.typography.bodySmall.copy(fontSize = TextUnit(10f, TextUnitType.Sp)),
        textAlign = TextAlign.Center,
    )
}
