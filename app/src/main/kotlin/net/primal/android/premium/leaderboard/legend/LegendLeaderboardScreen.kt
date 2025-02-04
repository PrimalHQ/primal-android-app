package net.primal.android.premium.leaderboard.legend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.api.model.LegendLeaderboardOrderBy
import net.primal.android.premium.leaderboard.domain.LeaderboardLegendEntry
import net.primal.android.premium.leaderboard.legend.ui.CONTRIBUTION_INDEX
import net.primal.android.premium.leaderboard.legend.ui.LATEST_INDEX
import net.primal.android.premium.leaderboard.legend.ui.LeaderboardTabs
import net.primal.android.premium.leaderboard.legend.ui.LegendLeaderboardItem
import net.primal.android.premium.leaderboard.legend.ui.PAGE_COUNT
import net.primal.android.theme.AppTheme

@Composable
fun LegendLeaderboardScreen(
    viewModel: LegendLeaderboardViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAboutLegendsClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LegendLeaderboardScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
        onProfileClick = onProfileClick,
        onAboutLegendsClick = onAboutLegendsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegendLeaderboardScreen(
    state: LegendLeaderboardContract.UiState,
    eventPublisher: (LegendLeaderboardContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onAboutLegendsClick: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val pagerState = rememberPagerState { PAGE_COUNT }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            eventPublisher(LegendLeaderboardContract.UiEvent.FetchLeaderboardByOrder(it.resolveOrderBy()))
        }
    }

    Scaffold(
        topBar = {
            LeaderboardTopAppBar(
                onBackClick = onClose,
                pagerState = pagerState,
                isActiveAccountLegend = state.isActiveAccountLegend,
                onAboutLegendsClick = onAboutLegendsClick,
            )
        },
    ) { paddingValues ->
        HorizontalPager(
            contentPadding = paddingValues,
            state = pagerState,
        ) { currentPage ->
            val entries = state.leaderboardEntries[currentPage.resolveOrderBy()] ?: emptyList()
            if (state.loading && entries.isEmpty()) {
                HeightAdjustableLoadingLazyListPlaceholder(height = 80.dp)
            } else if (state.error != null) {
                ListNoContent(
                    modifier = Modifier.fillMaxSize(),
                    noContentText = stringResource(id = R.string.premium_leaderboard_no_content),
                    onRefresh = {
                        eventPublisher(LegendLeaderboardContract.UiEvent.RetryFetch(currentPage.resolveOrderBy()))
                    },
                )
            } else {
                LeaderboardList(entries = entries, onProfileClick = onProfileClick)
            }
        }
    }
}

@Composable
private fun LeaderboardList(entries: List<LeaderboardLegendEntry>, onProfileClick: (String) -> Unit) {
    LazyColumn {
        itemsIndexed(
            items = entries,
            key = { index, item -> item.userId },
        ) { index, entry ->
            LegendLeaderboardItem(
                item = entry,
                index = index + 1,
                onClick = { onProfileClick(entry.userId) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LeaderboardTopAppBar(
    onBackClick: () -> Unit,
    isActiveAccountLegend: Boolean,
    pagerState: PagerState,
    onAboutLegendsClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column {
        PrimalTopAppBar(
            title = stringResource(id = R.string.premium_legend_leaderboard_title),
            navigationIcon = PrimalIcons.ArrowBack,
            onNavigationIconClick = onBackClick,
            showDivider = false,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeaderboardTabs(
                modifier = Modifier.weight(1f),
                selectedTabIndex = pagerState.currentPage,
                onLatestClick = { coroutineScope.launch { pagerState.animateScrollToPage(page = LATEST_INDEX) } },
                onContributionClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(page = CONTRIBUTION_INDEX) }
                },
            )

            if (!isActiveAccountLegend) {
                TextButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = onAboutLegendsClick,
                ) {
                    Text(
                        text = stringResource(id = R.string.premium_legend_leaderboard_about_legends),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.secondary,
                    )
                }
            }
        }

        PrimalDivider()
    }
}

private fun Int.resolveOrderBy() =
    if (this == LATEST_INDEX) LegendLeaderboardOrderBy.LastDonation else LegendLeaderboardOrderBy.DonatedBtc
