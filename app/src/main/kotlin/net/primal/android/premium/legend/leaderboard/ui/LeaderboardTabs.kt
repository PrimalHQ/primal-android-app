package net.primal.android.premium.legend.leaderboard.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.theme.AppTheme

internal const val PAGE_COUNT = 2
internal const val LATEST_INDEX = 0
internal const val CONTRIBUTION_INDEX = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onLatestClick: () -> Unit,
    onContributionClick: () -> Unit,
) {
    ScrollableTabRow(
        modifier = modifier.padding(vertical = 4.dp),
        edgePadding = 0.dp,
        selectedTabIndex = selectedTabIndex,
        containerColor = AppTheme.colorScheme.background,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(percent = 100)),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            PrimalSingleTab(
                selected = selectedTabIndex == LATEST_INDEX,
                onClick = onLatestClick,
                text = stringResource(id = R.string.premium_legend_leaderboard_latest_tab).uppercase(),
            )
            PrimalSingleTab(
                selected = selectedTabIndex == CONTRIBUTION_INDEX,
                onClick = onContributionClick,
                text = stringResource(id = R.string.premium_legend_leaderboard_contribution_tab).uppercase(),
            )

        }
    }
}
