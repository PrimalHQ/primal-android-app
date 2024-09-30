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
