package net.primal.android.feeds

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.feeds.ReadsFeedsContract.UiState.FeedMarketplaceStage
import net.primal.android.feeds.ui.DvmFeedDetails
import net.primal.android.feeds.ui.DvmFeedMarketplace
import net.primal.android.feeds.ui.FeedList
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadsFeedsBottomSheet(
    activeFeed: FeedUi,
    onFeedClick: (FeedUi) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val viewModel = hiltViewModel<ReadsFeedsViewModel, ReadsFeedsViewModel.Factory>(
        key = activeFeed.directive,
        creationCallback = { it.create(activeFeed) },
    )
    val uiState = viewModel.state.collectAsState()

    ReadsFeedsBottomSheet(
        state = uiState.value,
        onFeedClick = onFeedClick,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        eventPublisher = viewModel::setEvent,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun ReadsFeedsBottomSheet(
    state: ReadsFeedsContract.UiState,
    onFeedClick: (FeedUi) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    eventPublisher: (ReadsFeedsContract.UiEvent) -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        scrimColor = Color.Transparent,
        properties = ModalBottomSheetProperties(
            securePolicy = SecureFlagPolicy.Inherit,
            isFocusable = true,
            shouldDismissOnBackPress = false,
        ),
    ) {
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            targetState = state.feedMarketplaceStage,
            transitionSpec = { transitionSpecBetweenStages() },
            label = "FeedsModalBottomSheet",
        ) { target ->
            when (target) {
                FeedMarketplaceStage.FeedList ->
                    FeedList(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(id = R.string.reads_feeds_title),
                        feeds = state.feeds,
                        activeFeed = state.activeFeed,
                        onFeedClick = onFeedClick,
                        onEditFeedClick = { eventPublisher(ReadsFeedsContract.UiEvent.OpenEditMode) },
                        enableEditMode = true,
                        isEditMode = state.isEditMode,
                        onAddFeedClick = { eventPublisher(ReadsFeedsContract.UiEvent.ShowFeedMarketplace) },
                        onEditDoneClick = { eventPublisher(ReadsFeedsContract.UiEvent.CloseEditMode) },
                        onFeedReordered = { eventPublisher(ReadsFeedsContract.UiEvent.FeedReordered(feeds = it)) },
                        onFeedRemoved = { },
                    )

                FeedMarketplaceStage.FeedMarketplace -> {
                    DvmFeedMarketplace(
                        modifier = Modifier.fillMaxSize(),
                        dvmFeeds = state.dvmFeeds,
                        onFeedClick = { eventPublisher(ReadsFeedsContract.UiEvent.ShowFeedDetails(dvmFeed = it)) },
                        onClose = { eventPublisher(ReadsFeedsContract.UiEvent.CloseFeedMarketplace) },
                    )
                }

                FeedMarketplaceStage.FeedDetails -> {
                    var addedToFeeds by remember(state.selectedDvmFeed) {
                        mutableStateOf(state.feeds.map { it.directive }.contains(state.selectedDvmFeed?.dvmSpec))
                    }
                    DvmFeedDetails(
                        modifier = Modifier.fillMaxSize(),
                        dvmFeed = state.selectedDvmFeed,
                        addedToFeeds = addedToFeeds,
                        onClose = { eventPublisher(ReadsFeedsContract.UiEvent.CloseFeedDetails) },
                        onAddOrRemoveFeed = {
                            if (state.selectedDvmFeed != null) {
                                if (!addedToFeeds) {
                                    addedToFeeds = true
                                    eventPublisher(
                                        ReadsFeedsContract.UiEvent.AddDvmFeedToUserFeeds(
                                            dvmFeed = state.selectedDvmFeed,
                                        ),
                                    )
                                } else {
                                    addedToFeeds = true
                                    eventPublisher(
                                        ReadsFeedsContract.UiEvent.RemoveDvmFeedFromUserFeeds(
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
