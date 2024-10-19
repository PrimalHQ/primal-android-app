package net.primal.android.feeds.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
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
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.list.FeedListContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.list.ui.DvmFeedDetails
import net.primal.android.feeds.list.ui.DvmFeedMarketplace
import net.primal.android.feeds.list.ui.FeedList
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsBottomSheet(
    activeFeed: FeedUi,
    feedSpecKind: FeedSpecKind,
    onFeedClick: (FeedUi) -> Unit,
    onGoToWallet: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val viewModel = hiltViewModel<FeedListViewModel, FeedListViewModel.Factory>(
        key = activeFeed.spec,
        creationCallback = { it.create(activeFeed = activeFeed, specKind = feedSpecKind) },
    )
    val uiState = viewModel.state.collectAsState()

    FeedsBottomSheet(
        state = uiState.value,
        onFeedClick = onFeedClick,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        eventPublisher = viewModel::setEvent,
        onGoToWallet = onGoToWallet,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun FeedsBottomSheet(
    state: FeedListContract.UiState,
    onFeedClick: (FeedUi) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    eventPublisher: (FeedListContract.UiEvent) -> Unit,
    onGoToWallet: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
    ) {
        BackHandler {
            when (state.feedMarketplaceStage) {
                FeedMarketplaceStage.FeedList -> if (state.isEditMode) {
                    eventPublisher(FeedListContract.UiEvent.CloseEditMode)
                } else {
                    onDismissRequest()
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
            label = "FeedsBottomSheet",
        ) { target ->
            when (target) {
                FeedMarketplaceStage.FeedList ->
                    FeedList(
                        modifier = Modifier.fillMaxSize(),
                        title = when (state.specKind) {
                            FeedSpecKind.Reads -> stringResource(id = R.string.reads_feeds_title)
                            FeedSpecKind.Notes -> stringResource(id = R.string.home_feeds_title)
                        },
                        feeds = state.feeds,
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

                FeedMarketplaceStage.FeedMarketplace -> {
                    DvmFeedMarketplace(
                        modifier = Modifier.fillMaxSize(),
                        dvmFeeds = state.dvmFeeds,
                        onFeedClick = { eventPublisher(FeedListContract.UiEvent.ShowFeedDetails(dvmFeed = it)) },
                        onClose = { eventPublisher(FeedListContract.UiEvent.CloseFeedMarketplace) },
                        onGoToWallet = onGoToWallet,
                    )
                }

                FeedMarketplaceStage.FeedDetails -> {
                    var addedToFeeds by remember(state.selectedDvmFeed) {
                        val spec = state.selectedDvmFeed?.data?.buildSpec(specKind = state.specKind)
                        mutableStateOf(state.feeds.map { it.spec }.contains(spec))
                    }
                    DvmFeedDetails(
                        modifier = Modifier.fillMaxSize(),
                        dvmFeed = state.selectedDvmFeed,
                        addedToFeeds = addedToFeeds,
                        onGoToWallet = onGoToWallet,
                        onClose = { eventPublisher(FeedListContract.UiEvent.CloseFeedDetails) },
                        onAddOrRemoveFeed = {
                            if (state.selectedDvmFeed != null) {
                                if (!addedToFeeds) {
                                    addedToFeeds = true
                                    eventPublisher(
                                        FeedListContract.UiEvent.AddDvmFeedToUserFeeds(
                                            dvmFeed = state.selectedDvmFeed,
                                        ),
                                    )
                                } else {
                                    addedToFeeds = true
                                    eventPublisher(
                                        FeedListContract.UiEvent.RemoveDvmFeedFromUserFeeds(
                                            dvmFeed = state.selectedDvmFeed,
                                        ),
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
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
