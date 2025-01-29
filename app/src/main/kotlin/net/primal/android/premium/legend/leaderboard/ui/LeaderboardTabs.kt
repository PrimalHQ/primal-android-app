package net.primal.android.premium.legend.leaderboard.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.theme.AppTheme


internal const val PAGE_COUNT = 2
internal const val LATEST_INDEX = 0
internal const val CONTRIBUTION_INDEX = 1

@Composable
fun LeaderboardTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onLatestClick: () -> Unit,
    onContributionClick: () -> Unit,
) {
    TabRow(
        modifier = modifier.padding(vertical = 4.dp),
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
        PrimalSingleTab(
            selected = selectedTabIndex == LATEST_INDEX,
            onClick = onLatestClick,
            text = "Latest".uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == CONTRIBUTION_INDEX,
            onClick = onContributionClick,
            text = "Contribution".uppercase(),
        )
    }
}
