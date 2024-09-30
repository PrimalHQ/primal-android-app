package net.primal.android.explore.home.feeds

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.item.DvmFeedListItem
import net.primal.android.feeds.ui.DvmHeaderAndFeedList
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
    var showDvmFeedDetailsBottomSheet by remember { mutableStateOf(false) }
    var dvmFeedToShow by remember { mutableStateOf(state.feeds.firstOrNull()) }

    if (showDvmFeedDetailsBottomSheet) {
        dvmFeedToShow?.let {
            DvmFeedDetailsBottomSheet(
                onDismissRequest = { showDvmFeedDetailsBottomSheet = false },
                dvmFeed = it,
                addedToFeed = true,
            )
        }
    }

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
                    dvmFeedToShow = dvmFeed
                    showDvmFeedDetailsBottomSheet = true
                },
                showFollowsActionsAvatarRow = true,
            )
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
                            TextButton(
                                onClick = {},
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = AppTheme.colorScheme.onPrimary,
                                    contentColor = AppTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Text(
                                    text = "Add to home feeds",
                                    fontWeight = FontWeight.W600,
                                )
                            }
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
