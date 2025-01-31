package net.primal.android.premium.legend.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.api.model.LeaderboardOrderBy
import net.primal.android.premium.legend.leaderboard.ui.CONTRIBUTION_INDEX
import net.primal.android.premium.legend.leaderboard.ui.LATEST_INDEX
import net.primal.android.premium.legend.leaderboard.ui.LeaderboardTabs
import net.primal.android.premium.legend.leaderboard.ui.LegendLeaderboardItem
import net.primal.android.premium.legend.leaderboard.ui.PAGE_COUNT
import net.primal.android.theme.AppTheme

@Composable
fun LegendLeaderboardScreen(
    viewModel: LegendLeaderboardViewModel,
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAboutLegendsClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LegendLeaderboardScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onBackClick = onBackClick,
        onProfileClick = onProfileClick,
        onAboutLegendsClick = onAboutLegendsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendLeaderboardScreen(
    state: LegendLeaderboardContract.UiState,
    eventPublisher: (LegendLeaderboardContract.UiEvent) -> Unit,
    onBackClick: () -> Unit,
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
                onBackClick = onBackClick,
                pagerState = pagerState,
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
            } else {
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
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LeaderboardTopAppBar(
    onBackClick: () -> Unit,
    pagerState: PagerState,
    onAboutLegendsClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column {
        PrimalTopAppBar(
            title = stringResource(id = R.string.legend_leaderboard_title),
            titleFontWeight = FontWeight.Normal,
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

            TextButton(
                onClick = onAboutLegendsClick,
            ) {
                Text(
                    text = stringResource(id = R.string.legend_leaderboard_about_legends),
                    style = AppTheme.typography.bodySmall,
                )
            }
        }

        PrimalDivider()
    }
}

private fun Int.resolveOrderBy() =
    if (this == LATEST_INDEX) LeaderboardOrderBy.LastDonation else LeaderboardOrderBy.DonatedBtc
