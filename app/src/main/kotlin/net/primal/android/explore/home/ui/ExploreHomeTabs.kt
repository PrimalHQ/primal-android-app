package net.primal.android.explore.home.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.theme.AppTheme

internal const val EXPLORE_HOME_TAB_COUNT = 5

internal const val PEOPLE_INDEX = 0
internal const val FEEDS_INDEX = 1
internal const val ZAPS_INDEX = 2
internal const val MEDIA_INDEX = 3
internal const val TOPICS_INDEX = 4

@Composable
fun ExploreHomeTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onFeedsTabClick: () -> Unit,
    onPeopleTabClick: () -> Unit,
    onZapsTabClick: () -> Unit,
    onMediaTabClick: () -> Unit,
    onTopicsTabClick: () -> Unit,
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
            selected = selectedTabIndex == PEOPLE_INDEX,
            onClick = onPeopleTabClick,
            text = stringResource(id = R.string.explore_people_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == FEEDS_INDEX,
            onClick = onFeedsTabClick,
            text = stringResource(id = R.string.explore_feeds_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == ZAPS_INDEX,
            onClick = onZapsTabClick,
            text = stringResource(id = R.string.explore_zaps_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == MEDIA_INDEX,
            onClick = onMediaTabClick,
            text = stringResource(id = R.string.explore_media_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == TOPICS_INDEX,
            onClick = onTopicsTabClick,
            text = stringResource(id = R.string.explore_topics_tab).uppercase(),
        )
    }
}
