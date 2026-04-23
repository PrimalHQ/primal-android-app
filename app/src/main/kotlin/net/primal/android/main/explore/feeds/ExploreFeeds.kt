package net.primal.android.main.explore.feeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.errors.UiError
import net.primal.android.feeds.dvm.ui.DvmFeedDetailsBottomSheet
import net.primal.android.feeds.dvm.ui.DvmFeedListItem
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.theme.AppTheme
import net.primal.domain.feeds.buildSpec

@Composable
fun ExploreFeeds(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    val viewModel: ExploreFeedsViewModel = hiltViewModel<ExploreFeedsViewModel>()
    val uiState = viewModel.state.collectAsState()

    ExploreFeeds(
        modifier = modifier,
        state = uiState.value,
        paddingValues = paddingValues,
        eventPublisher = viewModel::setEvent,
        onGoToWallet = onGoToWallet,
        onUiError = onUiError,
    )
}

@Composable
fun ExploreFeeds(
    modifier: Modifier = Modifier,
    state: ExploreFeedsContract.UiState,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    eventPublisher: (ExploreFeedsContract.UiEvent) -> Unit,
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    var dvmFeedToShow by remember { mutableStateOf<DvmFeedUi?>(null) }

    dvmFeedToShow?.let { selectedDvmFeed ->
        val addedToFeed by remember(dvmFeedToShow, state.userFeedSpecs) {
            val kind = dvmFeedToShow?.data?.kind
            mutableStateOf(
                kind?.let { state.userFeedSpecs.contains(dvmFeedToShow?.data?.buildSpec(specKind = kind)) } ?: false,
            )
        }
        DvmFeedDetailsBottomSheet(
            onDismissRequest = {
                dvmFeedToShow?.let { eventPublisher(ExploreFeedsContract.UiEvent.ClearDvmFeed(it)) }
                dvmFeedToShow = null
            },
            dvmFeed = selectedDvmFeed,
            addedToFeed = addedToFeed,
            addToUserFeeds = { eventPublisher(ExploreFeedsContract.UiEvent.AddToUserFeeds(it)) },
            removeFromUserFeeds = { eventPublisher(ExploreFeedsContract.UiEvent.RemoveFromUserFeeds(it)) },
            onGoToWallet = onGoToWallet,
            onUiError = onUiError,
        )
    }

    if (state.loading && state.feeds.isEmpty()) {
        HeightAdjustableLoadingLazyListPlaceholder(
            modifier = modifier.fillMaxSize(),
            contentPaddingValues = paddingValues,
            clipShape = AppTheme.shapes.small,
            height = 100.dp,
        )
    } else if (state.feeds.isEmpty()) {
        ListNoContent(
            modifier = modifier.fillMaxSize(),
            noContentText = stringResource(id = R.string.explore_feeds_no_content),
            refreshButtonVisible = true,
            onRefresh = { eventPublisher(ExploreFeedsContract.UiEvent.RefreshFeeds) },
        )
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 12.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            items(
                items = state.feeds,
                key = { "${it.data.dvmPubkey}:${it.data.dvmId}" },
            ) { dvmFeed ->
                DvmFeedListItem(
                    modifier = Modifier.padding(top = 8.dp),
                    data = dvmFeed,
                    listItemContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    onFeedClick = { dvmFeedToShow = it },
                    showFollowsActionsAvatarRow = true,
                    onGoToWallet = onGoToWallet,
                    onUiError = onUiError,
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}
