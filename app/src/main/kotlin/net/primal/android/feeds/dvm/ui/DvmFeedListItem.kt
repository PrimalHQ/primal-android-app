package net.primal.android.feeds.dvm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedLikesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.FeedZapsFilled
import net.primal.android.core.errors.UiError
import net.primal.android.feeds.dvm.DvmFeedListItemContract
import net.primal.android.feeds.dvm.DvmFeedListItemViewModel
import net.primal.android.notes.feed.note.ui.SingleEventStat
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.zaps.canZap

private val PaidBackground = Color(0xFFFC6337)

@Composable
fun DvmFeedListItem(
    modifier: Modifier = Modifier,
    data: DvmFeedUi,
    listItemContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt2,
    avatarSize: Dp = 48.dp,
    extended: Boolean = false,
    showFollowsActionsAvatarRow: Boolean = false,
    clipShape: Shape? = AppTheme.shapes.small,
    onFeedClick: ((dvmFeed: DvmFeedUi) -> Unit)? = null,
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    val viewModel = hiltViewModel<DvmFeedListItemViewModel>()
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, uiState.value.error, onUiError) {
        uiState.value.error?.let { onUiError?.invoke(it) }
        viewModel.setEvent(DvmFeedListItemContract.UiEvent.DismissError)
    }

    DvmFeedListItem(
        modifier = modifier,
        onFeedClick = onFeedClick,
        eventPublisher = viewModel::setEvent,
        state = uiState.value,
        listItemContainerColor = listItemContainerColor,
        avatarSize = avatarSize,
        extended = extended,
        showFollowsActionsAvatarRow = showFollowsActionsAvatarRow,
        dvmFeed = data,
        clipShape = clipShape,
        onGoToWallet = onGoToWallet,
    )
}

@Composable
private fun DvmFeedListItem(
    modifier: Modifier = Modifier,
    dvmFeed: DvmFeedUi,
    state: DvmFeedListItemContract.UiState,
    onFeedClick: ((dvmFeed: DvmFeedUi) -> Unit)? = null,
    onGoToWallet: (() -> Unit)? = null,
    eventPublisher: (DvmFeedListItemContract.UiEvent) -> Unit,
    listItemContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt2,
    clipShape: Shape? = AppTheme.shapes.small,
    avatarSize: Dp = 48.dp,
    extended: Boolean = false,
    showFollowsActionsAvatarRow: Boolean = false,
) {
    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = state.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = { onGoToWallet?.invoke() },
        )
    }

    var showZapOptions by remember { mutableStateOf(false) }
    if (showZapOptions) {
        ZapBottomSheet(
            onDismissRequest = { showZapOptions = false },
            receiverName = dvmFeed.data.title,
            zappingState = state.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (state.zappingState.canZap(zapAmount)) {
                    eventPublisher(
                        DvmFeedListItemContract.UiEvent.OnZapClick(
                            dvmFeed = dvmFeed,
                            zapDescription = zapDescription,
                            zapAmount = zapAmount.toULong(),
                        ),
                    )
                } else {
                    showCantZapWarning = true
                }
            },
        )
    }
    Column(
        modifier = Modifier
            .run {
                if (clipShape != null) {
                    this.clip(clipShape)
                } else {
                    this
                }
            }
            .run {
                if (onFeedClick != null) {
                    this.clickable { onFeedClick(dvmFeed) }
                } else {
                    this
                }
            }
            .background(listItemContainerColor),
    ) {
        ListItem(
            modifier = modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(containerColor = listItemContainerColor),
            leadingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DvmFeedThumbnail(
                        avatarCdnImage = dvmFeed.data.avatarUrl?.let { CdnImage(sourceUrl = it) },
                        avatarSize = avatarSize,
                        isPrimal = dvmFeed.data.isPrimalFeed,
                        outlineColor = listItemContainerColor,
                    )
                    when {
                        dvmFeed.data.primalSubscriptionRequired == true -> SubBadge(width = avatarSize)
                        dvmFeed.data.isPaid -> PaidBadge(width = avatarSize)
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
                    text = dvmFeed.data.title,
                    maxLines = if (extended) 2 else 1,
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    if (dvmFeed.data.description != null) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            text = dvmFeed.data.description,
                            maxLines = if (extended) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (extended && dvmFeed.data.isPrimalFeed == true) {
                        CreatedByPrimalRow()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DvmFeedActionRow(
                            userLiked = dvmFeed.userLiked ?: false,
                            userZapped = dvmFeed.userZapped ?: false,
                            totalLikes = dvmFeed.totalLikes,
                            totalSatsZapped = dvmFeed.totalSatsZapped,
                            onLikeClick = {
                                eventPublisher(
                                    DvmFeedListItemContract.UiEvent.OnLikeClick(dvmFeed = dvmFeed),
                                )
                            },
                            onZapClick = {
                                if (state.zappingState.walletConnected) {
                                    showZapOptions = true
                                } else {
                                    showCantZapWarning = true
                                }
                            },
                        )
                        if (showFollowsActionsAvatarRow) {
                            val profileAvatarSize = 28.dp
                            val avatarsShown = dvmFeed.actionUserAvatars.size.coerceAtMost(MaxAvatarsToShow)

                            AvatarThumbnailsRow(
                                modifier = Modifier
                                    .size(
                                        width = profileAvatarSize * AvatarVisiblePercentage * (avatarsShown - 1) +
                                            profileAvatarSize,
                                        height = profileAvatarSize,
                                    ),
                                avatarCdnImages = dvmFeed.actionUserAvatars,
                                onClick = {},
                                maxAvatarsToShow = MaxAvatarsToShow,
                                displayAvatarOverflowIndicator = false,
                                avatarBorderColor = listItemContainerColor,
                                avatarSize = profileAvatarSize,
                                avatarOverlap = AvatarOverlap.Start,
                            )
                        }
                    }
                }
            },
        )
    }
}

private const val AvatarVisiblePercentage = 0.75f
private const val MaxAvatarsToShow = 5

@Composable
fun DvmFeedThumbnail(
    avatarCdnImage: CdnImage?,
    avatarSize: Dp,
    isPrimal: Boolean?,
    outlineColor: Color,
) {
    Box(
        modifier = Modifier.size(avatarSize),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AvatarThumbnail(
            avatarCdnImage = avatarCdnImage,
            avatarSize = avatarSize,
        )
        if (isPrimal == true) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(outlineColor)
                    .size(20.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.primal_wave_logo_summer),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colorScheme.background)
                        .size(17.dp),
                )
            }
        }
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
                        fontWeight = FontWeight.Normal,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    ),
                ) {
                    append(stringResource(id = R.string.dvm_feed_created_by))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" " + stringResource(id = R.string.app_name))
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
        text = stringResource(id = R.string.feed_marketplace_sub_feed_label).uppercase(),
        backgroundColor = AppTheme.colorScheme.tertiary,
        textColor = Color.White,
        width = width,
    )
}

@Composable
private fun PaidBadge(width: Dp) {
    Badge(
        text = stringResource(id = R.string.feed_marketplace_paid_feed_label).uppercase(),
        backgroundColor = PaidBackground,
        textColor = Color.White,
        width = width,
    )
}

@Composable
private fun FreeBadge(width: Dp) {
    Badge(
        text = stringResource(id = R.string.feed_marketplace_free_feed_label).uppercase(),
        backgroundColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        textColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
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
        style = AppTheme.typography.bodySmall,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
    )
}
