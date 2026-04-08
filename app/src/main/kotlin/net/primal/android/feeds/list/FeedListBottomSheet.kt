package net.primal.android.feeds.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.feeds.list.FeedListContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.list.ui.DvmFeedDetails
import net.primal.android.feeds.list.ui.DvmFeedMarketplace
import net.primal.android.feeds.list.ui.FeedList
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.feeds.buildSpec

@Composable
fun FeedsOverlayContent(
    activeFeed: FeedUi,
    feedSpecKind: FeedSpecKind,
    onFeedClick: (FeedUi) -> Unit,
    onGoToWallet: (() -> Unit)? = null,
) {
    val viewModel = hiltViewModel<FeedListViewModel, FeedListViewModel.Factory>(
        key = activeFeed.spec,
        creationCallback = { it.create(activeFeed = activeFeed, specKind = feedSpecKind) },
    )
    val uiState = viewModel.state.collectAsState()

    FeedsOverlayContent(
        state = uiState.value,
        onFeedClick = onFeedClick,
        eventPublisher = viewModel::setEvent,
        onGoToWallet = onGoToWallet,
    )
}

@Composable
private fun FeedsOverlayContent(
    state: FeedListContract.UiState,
    onFeedClick: (FeedUi) -> Unit,
    eventPublisher: (FeedListContract.UiEvent) -> Unit,
    onGoToWallet: (() -> Unit)? = null,
) {
    BackHandler {
        when (state.feedMarketplaceStage) {
            FeedMarketplaceStage.FeedList -> if (state.isEditMode) {
                eventPublisher(FeedListContract.UiEvent.CloseEditMode)
            }
            FeedMarketplaceStage.FeedMarketplace -> eventPublisher(FeedListContract.UiEvent.CloseFeedMarketplace)
            FeedMarketplaceStage.FeedDetails -> eventPublisher(FeedListContract.UiEvent.CloseFeedDetails)
        }
    }

    AnimatedContent(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        targetState = state.feedMarketplaceStage,
        transitionSpec = { transitionSpecBetweenStages() },
        label = "FeedsOverlay",
    ) { target ->
        when (target) {
            FeedMarketplaceStage.FeedList -> FeedListStage(
                state = state,
                onFeedClick = onFeedClick,
                eventPublisher = eventPublisher,
            )

            FeedMarketplaceStage.FeedMarketplace -> DvmFeedMarketplace(
                modifier = Modifier.fillMaxSize(),
                dvmFeeds = state.dvmFeeds,
                onFeedClick = { eventPublisher(FeedListContract.UiEvent.ShowFeedDetails(dvmFeed = it)) },
                onClose = { eventPublisher(FeedListContract.UiEvent.CloseFeedMarketplace) },
                onGoToWallet = onGoToWallet,
            )

            FeedMarketplaceStage.FeedDetails -> FeedDetailsStage(
                state = state,
                eventPublisher = eventPublisher,
                onGoToWallet = onGoToWallet,
            )
        }
    }
}

@Composable
private fun FeedListStage(
    state: FeedListContract.UiState,
    onFeedClick: (FeedUi) -> Unit,
    eventPublisher: (FeedListContract.UiEvent) -> Unit,
) {
    FeedList(
        modifier = Modifier.fillMaxSize(),
        title = when (state.specKind) {
                        FeedSpecKind.Reads -> stringResource(id = R.string.reads_feeds_title)
                        FeedSpecKind.Notes -> stringResource(id = R.string.home_feeds_title)
                    },feeds = state.feeds,
        activeFeed = state.activeFeed,
        onFeedClick = { feed ->
            if (state.isEditMode) {
                eventPublisher(
                    FeedListContract.UiEvent.UpdateFeedSpecEnabled(
                        feedSpec = feed.spec,
                        enabled = !feed.enabled,
                    ),
                )
            } else {
                onFeedClick(feed)
            }
        },
        onEditFeedClick = { eventPublisher(FeedListContract.UiEvent.OpenEditMode) },
        enableEditMode = true,
        isEditMode = state.isEditMode,
        onAddFeedClick = { eventPublisher(FeedListContract.UiEvent.ShowFeedMarketplace) },
        onEditDoneClick = { eventPublisher(FeedListContract.UiEvent.CloseEditMode) },
        onFeedReordered = { eventPublisher(FeedListContract.UiEvent.FeedReordered(feeds = it)) },
        onFeedEnabled = { feed, enabled ->
            eventPublisher(
                FeedListContract.UiEvent.UpdateFeedSpecEnabled(
                    feedSpec = feed.spec,
                    enabled = enabled,
                ),
            )
        },
        onFeedRemoved = {
            eventPublisher(FeedListContract.UiEvent.RemoveFeedFromUserFeeds(spec = it.spec))
        },
        onRestoreDefaultPrimalFeeds = {
            eventPublisher(FeedListContract.UiEvent.RestoreDefaultPrimalFeeds)
        },
    )
}

@Composable
private fun FeedDetailsStage(
    state: FeedListContract.UiState,
    eventPublisher: (FeedListContract.UiEvent) -> Unit,
    onGoToWallet: (() -> Unit)?,
) {
    val localSelectedFeed by remember(state.selectedDvmFeed) {
        val spec = state.selectedDvmFeed?.data?.buildSpec(specKind = state.specKind)
        val foundFeed = state.feeds.firstOrNull { it.spec == spec }
        mutableStateOf(foundFeed)
    }
    DvmFeedDetails(
        modifier = Modifier.fillMaxSize(),
        dvmFeed = state.selectedDvmFeed,
        localFeed = localSelectedFeed,
        onGoToWallet = onGoToWallet,
        onClose = { eventPublisher(FeedListContract.UiEvent.CloseFeedDetails) },
        onAddOrRemoveFeed = {
            if (state.selectedDvmFeed != null) {
                val localFeed = localSelectedFeed
                when {
                    localFeed == null -> eventPublisher(
                        FeedListContract.UiEvent.AddDvmFeedToUserFeeds(dvmFeed = state.selectedDvmFeed),
                    )
                    localFeed.deletable -> eventPublisher(
                        FeedListContract.UiEvent.RemoveDvmFeedFromUserFeeds(dvmFeed = state.selectedDvmFeed),
                    )
                    localFeed.enabled -> eventPublisher(
                        FeedListContract.UiEvent.DisableFeedInUserFeeds(spec = localFeed.spec),
                    )
                    else -> eventPublisher(
                        FeedListContract.UiEvent.EnableFeedInUserFeeds(spec = localFeed.spec),
                    )
                }
            }
        },
    )
}

private fun AnimatedContentTransitionScope<FeedMarketplaceStage>.transitionSpecBetweenStages() =
    when (initialState) {
        FeedMarketplaceStage.FeedList -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }

        FeedMarketplaceStage.FeedMarketplace -> {
            when (targetState) {
                FeedMarketplaceStage.FeedList -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
                }

                FeedMarketplaceStage.FeedMarketplace, FeedMarketplaceStage.FeedDetails -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }
            }
        }

        FeedMarketplaceStage.FeedDetails -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }
    }
