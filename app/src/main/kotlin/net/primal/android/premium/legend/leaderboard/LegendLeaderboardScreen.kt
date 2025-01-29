package net.primal.android.premium.legend.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
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
import kotlinx.coroutines.launch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.api.model.LeaderboardOrderBy
import net.primal.android.premium.legend.leaderboard.ui.CONTRIBUTION_INDEX
import net.primal.android.premium.legend.leaderboard.ui.LATEST_INDEX
import net.primal.android.premium.legend.leaderboard.ui.LeaderboardTabs
import net.primal.android.premium.legend.leaderboard.ui.PAGE_COUNT
import net.primal.android.premium.legend.model.LegendLeaderboardEntry

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
        onBackClick = onBackClick,
        onProfileClick = onProfileClick,
        onAboutLegendsClick = onAboutLegendsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendLeaderboardScreen(
    state: LegendLeaderboardContract.UiState,
    onBackClick: () -> Unit,
    onAboutLegendsClick: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = "Primal Legends",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBackClick,
                showDivider = false,
            )
        },
    ) { paddingValues ->
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState { PAGE_COUNT }

        Row(
            modifier = Modifier.padding(paddingValues),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeaderboardTabs(
                selectedTabIndex = pagerState.currentPage,
                onLatestClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(page = LATEST_INDEX) }
                },
                onContributionClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page = CONTRIBUTION_INDEX)
                    }
                },
            )

            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onAboutLegendsClick,
            ) {
                Text(text = "About Legends")
            }
        }

        HorizontalPager(
            state = pagerState,
        ) { currentPage ->
            LazyColumn {
                val entries = state.leaderboardEntries[currentPage.resolveOrderBy()] ?: emptyList()
                items(
                    items = entries,
                    key = { it.userId },
                ) { entry ->
                    LegendLeaderboardItem(
                        item = entry,
                    )
                }
            }
        }
    }
}

@Composable
fun LegendLeaderboardItem(
    modifier: Modifier = Modifier,
    item: LegendLeaderboardEntry,
) {
    Text(text = item.displayName ?: "no")

}

private fun Int.resolveOrderBy() =
    if (this == LATEST_INDEX) LeaderboardOrderBy.LastDonation else LeaderboardOrderBy.DonatedBtc
