package net.primal.android.stats.reactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.stats.reactions.ReactionsContract
import net.primal.android.stats.reactions.ReactionsViewModel
import net.primal.android.theme.AppTheme

private const val REACTION_TABS_COUNT = 3
private const val ZAPS_TAB_INDEX = 0
private const val LIKES_TAB_INDEX = 1
private const val REPOSTS_TAB_INDEX = 2

@Composable
fun ReactionsScreen(
    viewModel: ReactionsViewModel,
    onClose: () -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    ReactionsScreen(
        state = state.value,
        onClose = onClose,
        onProfileClick = onProfileClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReactionsScreen(
    state: ReactionsContract.UiState,
    onClose: () -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { REACTION_TABS_COUNT }

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.note_reactions_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                showDivider = true,
                footer = {
                    ReactionsTabs(
                        selectedTabIndex = pagerState.currentPage,
                        onZapsClick = {
                            uiScope.launch {
                                pagerState.animateScrollToPage(
                                    ZAPS_TAB_INDEX,
                                )
                            }
                        },
                        onLikesClick = {
                            uiScope.launch {
                                pagerState.animateScrollToPage(
                                    LIKES_TAB_INDEX,
                                )
                            }
                        },
                        onRepostsClick = {
                            uiScope.launch {
                                pagerState.animateScrollToPage(
                                    REPOSTS_TAB_INDEX,
                                )
                            }
                        },
                    )
                },
            )
        },
        content = { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
            ) { currentPage ->
                when (currentPage) {
                    ZAPS_TAB_INDEX -> {
                        ReactionsZapsLazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            state = state,
                            onProfileClick = onProfileClick,
                        )
                    }

                    LIKES_TAB_INDEX -> {
                        GenericReactionsLazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            reactions = state.likes,
                            reactionIcon = PrimalIcons.FeedLikes,
                            loading = state.loading,
                            noContentText = stringResource(R.string.note_reactions_likes_no_content),
                            onProfileClick = onProfileClick,
                        )
                    }

                    REPOSTS_TAB_INDEX -> {
                        GenericReactionsLazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            reactions = state.reposts,
                            reactionIcon = PrimalIcons.FeedReposts,
                            loading = state.loading,
                            noContentText = stringResource(R.string.note_reactions_reposts_no_content),
                            onProfileClick = onProfileClick,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun ReactionsTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onZapsClick: () -> Unit,
    onLikesClick: () -> Unit,
    onRepostsClick: () -> Unit,
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(percent = 100)),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
        divider = { },
    ) {
        PrimalSingleTab(
            selected = selectedTabIndex == ZAPS_TAB_INDEX,
            text = stringResource(id = R.string.note_reactions_zaps_tab).uppercase(),
            onClick = onZapsClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
        PrimalSingleTab(
            selected = selectedTabIndex == LIKES_TAB_INDEX,
            text = stringResource(id = R.string.note_reactions_likes_tab).uppercase(),
            onClick = onLikesClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
        PrimalSingleTab(
            selected = selectedTabIndex == REPOSTS_TAB_INDEX,
            text = stringResource(id = R.string.note_reactions_reposts_tab).uppercase(),
            onClick = onRepostsClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
    }
}
