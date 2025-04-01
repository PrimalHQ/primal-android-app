package net.primal.android.premium.leaderboard.ogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.isEmpty
import net.primal.android.premium.leaderboard.legend.ui.LATEST_INDEX
import net.primal.android.premium.leaderboard.ogs.ui.OGLeaderboardItem
import net.primal.android.premium.leaderboard.ogs.ui.OGLeaderboardTabs
import net.primal.android.premium.leaderboard.ogs.ui.PAGE_COUNT
import net.primal.android.theme.AppTheme
import net.primal.domain.OGLeaderboardEntry
import timber.log.Timber

@Composable
fun OGLeaderboardScreen(
    viewModel: OGLeaderboardViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    onGetPrimalPremiumClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    OGLeaderboardScreen(
        state = uiState.value,
        onClose = onClose,
        onProfileClick = onProfileClick,
        onGetPrimalPremiumClick = onGetPrimalPremiumClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OGLeaderboardScreen(
    state: OGLeaderboardContract.UiState,
    onClose: () -> Unit,
    onGetPrimalPremiumClick: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val pagerState = rememberPagerState { PAGE_COUNT }

    Scaffold(
        topBar = {
            LeaderboardTopAppBar(
                onBackClick = onClose,
                isActiveAccountPremium = state.isActiveAccountPremium,
                pagerState = pagerState,
                onGetPrimalPremiumClick = onGetPrimalPremiumClick,
            )
        },
    ) { paddingValues ->
        val pagingItems = state.leaderboardEntries.collectAsLazyPagingItems()
        val lazyListState = pagingItems.rememberLazyListStatePagingWorkaround()
        HorizontalPager(
            contentPadding = paddingValues,
            state = pagerState,
        ) { currentPage ->
            if (pagingItems.isEmpty()) {
                when (val refreshLoadState = pagingItems.loadState.refresh) {
                    is LoadState.Error -> {
                        Timber.w(refreshLoadState.error)
                        ListNoContent(
                            modifier = Modifier.fillMaxSize(),
                            noContentText = stringResource(id = R.string.premium_leaderboard_error_loading),
                            onRefresh = { pagingItems.refresh() },
                        )
                    }

                    is LoadState.NotLoading -> {
                        if (pagingItems.loadState.isIdle) {
                            ListNoContent(
                                modifier = Modifier.fillMaxSize(),
                                noContentText = stringResource(id = R.string.premium_leaderboard_no_content),
                                onRefresh = { pagingItems.refresh() },
                            )
                        }
                    }

                    LoadState.Loading -> {
                        HeightAdjustableLoadingLazyListPlaceholder(height = 80.dp)
                    }
                }
            } else {
                LeaderboardList(
                    lazyListState = lazyListState,
                    entries = pagingItems,
                    onProfileClick = onProfileClick,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardList(
    lazyListState: LazyListState,
    entries: LazyPagingItems<OGLeaderboardEntry>,
    onProfileClick: (String) -> Unit,
) {
    LazyColumn(state = lazyListState) {
        items(
            count = entries.itemCount,
        ) { index ->
            val entry = entries[index]

            entry?.let {
                OGLeaderboardItem(
                    item = entry,
                    onClick = { onProfileClick(entry.userId) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LeaderboardTopAppBar(
    pagerState: PagerState,
    isActiveAccountPremium: Boolean,
    onBackClick: () -> Unit,
    onGetPrimalPremiumClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column {
        PrimalTopAppBar(
            title = stringResource(id = R.string.premium_ogs_leaderboard_title),
            navigationIcon = PrimalIcons.ArrowBack,
            onNavigationIconClick = onBackClick,
            showDivider = false,
        )
        if (!isActiveAccountPremium) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OGLeaderboardTabs(
                    modifier = Modifier.weight(1f),
                    selectedTabIndex = pagerState.currentPage,
                    onLatestClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(page = LATEST_INDEX) }
                    },
                )

                TextButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = onGetPrimalPremiumClick,
                ) {
                    Text(
                        text = stringResource(id = R.string.premium_ogs_leaderboard_get_primal_premium),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.secondary,
                    )
                }
            }
        }

        PrimalDivider()
    }
}
