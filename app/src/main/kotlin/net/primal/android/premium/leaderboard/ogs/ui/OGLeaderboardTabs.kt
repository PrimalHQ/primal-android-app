package net.primal.android.premium.leaderboard.ogs.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.theme.AppTheme

internal const val PAGE_COUNT = 1
internal const val LATEST_INDEX = 0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OGLeaderboardTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onLatestClick: () -> Unit,
) {
    SecondaryScrollableTabRow(
        modifier = modifier.padding(vertical = 4.dp),
        edgePadding = 0.dp,
        selectedTabIndex = selectedTabIndex,
        containerColor = AppTheme.colorScheme.background,
        divider = {},
        indicator = {
            if (selectedTabIndex in 0 until PAGE_COUNT) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedTabIndex)
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
                text = stringResource(id = R.string.premium_ogs_leaderboard_latest_tab).uppercase(),
            )
        }
    }
}
