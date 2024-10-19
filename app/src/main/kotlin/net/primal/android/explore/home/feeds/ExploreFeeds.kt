package net.primal.android.explore.home.feeds

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.item.DvmFeedListItem
import net.primal.android.feeds.ui.DvmHeaderAndFeedList
import net.primal.android.theme.AppTheme

@Composable
fun ExploreFeeds(modifier: Modifier = Modifier, paddingValues: PaddingValues = PaddingValues(all = 0.dp)) {
    val viewModel: ExploreFeedsViewModel = hiltViewModel<ExploreFeedsViewModel>()
    val uiState = viewModel.state.collectAsState()

    ExploreFeeds(
        modifier = modifier,
        state = uiState.value,
        paddingValues = paddingValues,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
fun ExploreFeeds(
    modifier: Modifier = Modifier,
    state: ExploreFeedsContract.UiState,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    eventPublisher: (ExploreFeedsContract.UiEvent) -> Unit,
) {
    var dvmFeedToShow by remember { mutableStateOf<DvmFeed?>(null) }

    dvmFeedToShow?.let {
        val addedToFeed by remember(dvmFeedToShow, state.userFeedSpecs) {
            val kind = dvmFeedToShow?.kind
            mutableStateOf(
                kind?.let { state.userFeedSpecs.contains(dvmFeedToShow?.buildSpec(specKind = kind)) } ?: false,
            )
        }
        DvmFeedDetailsBottomSheet(
            onDismissRequest = { dvmFeedToShow = null },
            dvmFeed = it,
            addedToFeed = addedToFeed,
            addToUserFeeds = { eventPublisher(ExploreFeedsContract.UiEvent.AddToUserFeeds(it)) },
            removeFromUserFeeds = { eventPublisher(ExploreFeedsContract.UiEvent.RemoveFromUserFeeds(it)) },
        )
    }

    if (state.loading && state.feeds.isEmpty() || true) {
        HeightAdjustableLoadingListPlaceholder(
            modifier = Modifier.fillMaxSize(),
            contentPaddingValues = paddingValues,
            clipShape = AppTheme.shapes.small,
            height = 100.dp,
        )
    } else if (state.feeds.isEmpty()) {
        ListNoContent(
            modifier = Modifier.fillMaxSize(),
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
                key = { "${it.dvmPubkey}:${it.dvmId}" },
            ) { dvmFeed ->
                DvmFeedListItem(
                    modifier = Modifier.padding(top = 8.dp),
                    data = dvmFeed,
                    listItemContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    onFeedClick = { dvmFeedToShow = it },
                    showFollowsActionsAvatarRow = true,
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DvmFeedDetailsBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    dvmFeed: DvmFeed,
    addedToFeed: Boolean,
    addToUserFeeds: (DvmFeed) -> Unit,
    removeFromUserFeeds: (DvmFeed) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val handleCloseBottomSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }

    BackHandler {
        handleCloseBottomSheet()
    }

    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .padding(vertical = 16.dp),
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = handleCloseBottomSheet) {
                                Icon(
                                    imageVector = PrimalIcons.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                        actions = {
                            ActionButton(
                                addedToFeed = addedToFeed,
                                addToUserFeeds = { addToUserFeeds(dvmFeed) },
                                removeFromUserFeeds = { removeFromUserFeeds(dvmFeed) },
                            )
                        },
                    )
                    PrimalDivider()
                }
            },
        ) { paddingValues ->
            DvmHeaderAndFeedList(
                modifier = Modifier.padding(paddingValues),
                dvmFeed = dvmFeed,
                extended = true,
                showFollowsActionsAvatarRow = true,
                clipShape = null,
            )
        }
    }
}

@Composable
private fun ActionButton(
    addedToFeed: Boolean,
    addToUserFeeds: () -> Unit,
    removeFromUserFeeds: () -> Unit,
) {
    if (addedToFeed) {
        TextButton(
            onClick = removeFromUserFeeds,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(id = R.string.explore_feeds_dvm_details_action_button_remove),
                fontWeight = FontWeight.W600,
            )
        }
    } else {
        TextButton(
            onClick = addToUserFeeds,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = AppTheme.colorScheme.onPrimary,
                contentColor = AppTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                text = stringResource(id = R.string.explore_feeds_dvm_details_action_button_add),
                fontWeight = FontWeight.W600,
            )
        }
    }
}
