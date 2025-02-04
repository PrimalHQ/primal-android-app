package net.primal.android.premium.leaderboard.ogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import net.primal.android.premium.api.model.LeaderboardOrderBy
import net.primal.android.premium.leaderboard.legend.ui.LATEST_INDEX
import net.primal.android.premium.leaderboard.ogs.ui.OGLeaderboardItem
import net.primal.android.premium.leaderboard.ogs.ui.OGLeaderboardTabs
import net.primal.android.premium.leaderboard.ogs.ui.PAGE_COUNT
import net.primal.android.premium.leaderboard.domain.LeaderboardLegendEntry
import net.primal.android.theme.AppTheme

@Composable
fun OGLeaderboardScreen(
    viewModel: OGLeaderboardViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAboutOGsClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    OGLeaderboardScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
        onProfileClick = onProfileClick,
        onAboutOGsClick = onAboutOGsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OGLeaderboardScreen(
    state: OGLeaderboardContract.UiState,
    eventPublisher: (OGLeaderboardContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onAboutOGsClick: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val pagerState = rememberPagerState { PAGE_COUNT }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            eventPublisher(OGLeaderboardContract.UiEvent.FetchLeaderboardByOrder(it.resolveOrderBy()))
        }
    }

    Scaffold(
        topBar = {
            LeaderboardTopAppBar(
                onBackClick = onClose,
                pagerState = pagerState,
                onAboutOGsClick = onAboutOGsClick,
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
                        eventPublisher(OGLeaderboardContract.UiEvent.RetryFetch(currentPage.resolveOrderBy()))
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
        items(
            items = entries,
            key = { it.userId },
        ) { entry ->
            OGLeaderboardItem(
                item = entry,
                onClick = { onProfileClick(entry.userId) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LeaderboardTopAppBar(
    onBackClick: () -> Unit,
    pagerState: PagerState,
    onAboutOGsClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column {
        PrimalTopAppBar(
            title = stringResource(id = R.string.premium_ogs_leaderboard_title),
            navigationIcon = PrimalIcons.ArrowBack,
            onNavigationIconClick = onBackClick,
            showDivider = false,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OGLeaderboardTabs(
                modifier = Modifier.weight(1f),
                selectedTabIndex = pagerState.currentPage,
                onLatestClick = { coroutineScope.launch { pagerState.animateScrollToPage(page = LATEST_INDEX) } },
            )

            TextButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = onAboutOGsClick,
            ) {
                Text(
                    text = stringResource(id = R.string.premium_ogs_leaderboard_about_ogs),
                    style = AppTheme.typography.bodySmall,
                )
            }
        }

        PrimalDivider()
    }
}

private fun Int.resolveOrderBy() =
    if (this == LATEST_INDEX) LeaderboardOrderBy.LastDonation else LeaderboardOrderBy.DonatedBtc
