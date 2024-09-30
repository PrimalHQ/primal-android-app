package net.primal.android.explore.home.feeds

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedNewLike
import net.primal.android.core.compose.icons.primaliconpack.FeedNewLikeFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZap
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZapFilled
import net.primal.android.core.compose.res.painterResource
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.item.DvmFeedListItem
import net.primal.android.notes.feed.note.ui.SingleEventStat
import net.primal.android.notes.feed.note.ui.toPostStatString
import net.primal.android.theme.AppTheme

@Composable
fun ExploreFeeds(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    activeAccountPubkey: String?,
) {
    val viewModel: ExploreFeedsViewModel =
        hiltViewModel<ExploreFeedsViewModel, ExploreFeedsViewModel.Factory> { factory ->
            factory.create(activeAccountPubkey = activeAccountPubkey)
        }
    val uiState = viewModel.state.collectAsState()

    ExploreFeeds(
        modifier = modifier,
        state = uiState.value,
        paddingValues = paddingValues,
    )
}

@Composable
fun ExploreFeeds(
    modifier: Modifier = Modifier,
    state: ExploreFeedsContract.UiState,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = state.feeds,
        ) { dvmFeed ->
            DvmFeedListItem(
                modifier = Modifier.padding(top = 8.dp),
                data = dvmFeed,
                listItemContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
                onFeedClick = { dvmFeed ->
                    when (dvmFeed.kind) {
                        FeedSpecKind.Reads -> TODO()
                        FeedSpecKind.Notes -> TODO()
                        null -> {}
                    }
                },
                showFollowsActionsAvatarRow = true,
            )
        }
    }
}

@Composable
private fun DvmFeedColumn(modifier: Modifier = Modifier, data: DvmFeed) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.small)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
    ) {
        ListItem(
            modifier = Modifier.padding(top = 8.dp),
            colors = ListItemDefaults.colors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
            ),
            leadingContent = {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(size = 52.dp),
                ) {
                    AsyncImage(
                        model = data.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(size = 52.dp)
                            .clip(CircleShape),
                    )
                    // TODO(marko): fix this later
                    Box {
                        if (data.primalSubscriptionRequired == true) {
                            Image(
                                modifier = Modifier
                                    .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
                                    .fillMaxSize(),
                                painter = painterResource(
                                    darkResId = R.drawable.primal_wave_logo_summer,
                                    lightResId = R.drawable.primal_wave_logo_summer,
                                ),
                                contentDescription = null,
                            )
                        }
                    }
                }
            },
            headlineContent = {
                Text(
                    text = data.title,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = data.description ?: "",
                    style = AppTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            },
        )
        Row(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (data.isPaid) {
//                PaidBadge()
            } else {
//                FreeBadge()
            }
            DvmFeedActionRow(
                totalLikes = data.totalLikes,
                totalSatsZapped = data.totalSatsZapped,
            )
        }
    }
}

@Composable
private fun DvmFeedActionRow(totalLikes: Long?, totalSatsZapped: Long?) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val iconSize = 20.sp
    Row(
        modifier = Modifier.padding(start = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SingleEventStat(
            textCount = totalLikes?.toPostStatString(numberFormat = numberFormat) ?: "",
            highlighted = false,
            iconSize = iconSize,
            iconVector = PrimalIcons.FeedNewLike,
            iconVectorHighlight = PrimalIcons.FeedNewLikeFilled,
            colorHighlight = AppTheme.extraColorScheme.liked,
            onClick = {},
            iconContentDescription = stringResource(id = R.string.accessibility_likes_count),
        )
        SingleEventStat(
            textCount = totalSatsZapped?.toPostStatString(numberFormat) ?: "",
            highlighted = false,
            iconVector = PrimalIcons.FeedNewZap,
            iconSize = iconSize,
            iconVectorHighlight = PrimalIcons.FeedNewZapFilled,
            colorHighlight = AppTheme.extraColorScheme.zapped,
            onClick = {},
            iconContentDescription = stringResource(id = R.string.accessibility_zaps_count),
        )
    }
}
